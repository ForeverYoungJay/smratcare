package com.zhiyangyun.care.chat.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.chat.entity.ChatConversation;
import com.zhiyangyun.care.chat.entity.ChatMember;
import com.zhiyangyun.care.chat.entity.ChatMessage;
import com.zhiyangyun.care.chat.entity.ChatTaskLink;
import com.zhiyangyun.care.chat.mapper.ChatConversationMapper;
import com.zhiyangyun.care.chat.mapper.ChatMemberMapper;
import com.zhiyangyun.care.chat.mapper.ChatMessageMapper;
import com.zhiyangyun.care.chat.mapper.ChatTaskLinkMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.realtime.OaQuickChatWebSocketHandler;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协同消息：服务端保存会话、成员与消息，每个请求都按当前登录员工的成员身份校验。
 * 消息转待办只创建统一任务（oa_task）并记录来源关联，不另建任务主表；
 * 任务办理人不因此获得会话的聊天记录访问权。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
  private static final int MAX_BODY = 2000;
  private static final Duration RECALL_WINDOW = Duration.ofMinutes(2);

  private final ChatConversationMapper conversationMapper;
  private final ChatMemberMapper memberMapper;
  private final ChatMessageMapper messageMapper;
  private final ChatTaskLinkMapper linkMapper;
  private final OaTaskMapper taskMapper;
  private final StaffMapper staffMapper;
  private final OaQuickChatWebSocketHandler socket;

  public ChatController(ChatConversationMapper conversationMapper, ChatMemberMapper memberMapper,
      ChatMessageMapper messageMapper, ChatTaskLinkMapper linkMapper, OaTaskMapper taskMapper,
      StaffMapper staffMapper, OaQuickChatWebSocketHandler socket) {
    this.conversationMapper = conversationMapper;
    this.memberMapper = memberMapper;
    this.messageMapper = messageMapper;
    this.linkMapper = linkMapper;
    this.taskMapper = taskMapper;
    this.staffMapper = staffMapper;
    this.socket = socket;
  }

  // ---------- 会话 ----------

  /** 我参与的会话（含未读数）；已退出的会话不再出现 */
  @GetMapping("/conversations")
  public Result<List<Map<String, Object>>> conversations() {
    Long me = AuthContext.getStaffId();
    List<ChatMember> mine = memberMapper.selectList(Wrappers.lambdaQuery(ChatMember.class)
        .eq(ChatMember::getStaffId, me).isNull(ChatMember::getLeftAt));
    if (mine.isEmpty()) {
      return Result.ok(List.of());
    }
    Map<Long, ChatMember> byConv = mine.stream().collect(Collectors.toMap(ChatMember::getConversationId, Function.identity(), (a, b) -> a));
    List<ChatConversation> convs = conversationMapper.selectList(Wrappers.lambdaQuery(ChatConversation.class)
        .in(ChatConversation::getId, byConv.keySet()).isNull(ChatConversation::getArchivedAt)
        .orderByDesc(ChatConversation::getLastMessageAt));
    Map<Long, String> directNames = directPeerNames(convs, me);
    List<Map<String, Object>> rows = new ArrayList<>();
    for (ChatConversation c : convs) {
      ChatMember member = byConv.get(c.getId());
      long unread = messageMapper.selectCount(Wrappers.lambdaQuery(ChatMessage.class)
          .eq(ChatMessage::getConversationId, c.getId()).ne(ChatMessage::getSenderId, me)
          .gt(member.getLastReadMessageId() != null, ChatMessage::getId, member.getLastReadMessageId()));
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("id", String.valueOf(c.getId()));
      row.put("type", c.getConvType());
      row.put("title", "DIRECT".equals(c.getConvType()) ? directNames.getOrDefault(c.getId(), "单聊") : c.getTitle());
      row.put("lastMessagePreview", c.getLastMessagePreview());
      row.put("lastMessageAt", c.getLastMessageAt());
      row.put("unread", unread);
      row.put("muted", Integer.valueOf(1).equals(member.getMuted()));
      row.put("myRole", member.getMemberRole());
      rows.add(row);
    }
    return Result.ok(rows);
  }

  public record CreateConversationRequest(String type, String title, List<Long> memberIds) {}

  /** 单聊：同一对员工只建一个会话；群聊：创建人为群主，成员必须是本机构在职员工 */
  @PostMapping("/conversations")
  @Transactional
  public Result<Map<String, Object>> createConversation(@RequestBody CreateConversationRequest request) {
    Long me = AuthContext.getStaffId();
    Long orgId = AuthContext.getOrgId();
    String type = "GROUP".equalsIgnoreCase(request.type()) ? "GROUP" : "DIRECT";
    Set<Long> others = new LinkedHashSet<>(request.memberIds() == null ? List.of() : request.memberIds());
    others.remove(me);
    List<StaffAccount> staff = activeStaff(orgId, others);
    if (staff.size() != others.size() || others.isEmpty()) {
      throw new IllegalArgumentException("成员必须是本机构在职员工");
    }
    if ("DIRECT".equals(type)) {
      if (others.size() != 1) {
        throw new IllegalArgumentException("单聊只能选择一位同事");
      }
      Long peer = others.iterator().next();
      String key = Math.min(me, peer) + "_" + Math.max(me, peer);
      ChatConversation existing = conversationMapper.selectOne(Wrappers.lambdaQuery(ChatConversation.class)
          .eq(ChatConversation::getOrgId, orgId).eq(ChatConversation::getDirectKey, key).last("LIMIT 1"));
      if (existing != null) {
        return Result.ok(Map.of("id", String.valueOf(existing.getId())));
      }
      ChatConversation conv = newConversation(orgId, "DIRECT", null, me);
      conv.setDirectKey(key);
      conversationMapper.insert(conv);
      addMember(conv.getId(), me, "MEMBER");
      addMember(conv.getId(), peer, "MEMBER");
      return Result.ok(Map.of("id", String.valueOf(conv.getId())));
    }
    String title = request.title() == null ? "" : request.title().trim();
    if (title.isEmpty() || title.length() > 100) {
      throw new IllegalArgumentException("请填写群名称（100 字以内）");
    }
    ChatConversation conv = newConversation(orgId, "GROUP", title, me);
    conversationMapper.insert(conv);
    addMember(conv.getId(), me, "OWNER");
    others.forEach(id -> addMember(conv.getId(), id, "MEMBER"));
    postSystem(conv, me, AuthContext.getUsername() + " 创建了群聊");
    return Result.ok(Map.of("id", String.valueOf(conv.getId())));
  }

  @GetMapping("/conversations/{id}/members")
  public Result<List<Map<String, Object>>> members(@PathVariable Long id) {
    requireMember(id);
    List<ChatMember> members = activeMembers(id);
    Map<Long, StaffAccount> staff = staffMap(members.stream().map(ChatMember::getStaffId).toList());
    return Result.ok(members.stream().map(m -> {
      Map<String, Object> row = new LinkedHashMap<>();
      StaffAccount s = staff.get(m.getStaffId());
      row.put("staffId", String.valueOf(m.getStaffId()));
      row.put("name", s == null ? "已离职" : s.getRealName());
      row.put("role", m.getMemberRole());
      return row;
    }).toList());
  }

  public record MembersRequest(List<Long> add, List<Long> remove) {}

  /** 群主/管理员管理成员；移出后立即失去消息访问权（历史消息仍保留在服务端） */
  @PostMapping("/conversations/{id}/members")
  @Transactional
  public Result<Void> manageMembers(@PathVariable Long id, @RequestBody MembersRequest request) {
    ChatMember me = requireMember(id);
    ChatConversation conv = conversationMapper.selectById(id);
    if (!"GROUP".equals(conv.getConvType()) || !List.of("OWNER", "ADMIN").contains(me.getMemberRole())) {
      throw new org.springframework.security.access.AccessDeniedException("只有群主或群管理员可以管理成员");
    }
    List<Long> add = request.add() == null ? List.of() : request.add();
    if (!add.isEmpty() && activeStaff(conv.getOrgId(), add).size() != new LinkedHashSet<>(add).size()) {
      throw new IllegalArgumentException("成员必须是本机构在职员工");
    }
    for (Long staffId : add) {
      ChatMember existing = memberMapper.selectOne(Wrappers.lambdaQuery(ChatMember.class)
          .eq(ChatMember::getConversationId, id).eq(ChatMember::getStaffId, staffId));
      if (existing == null) {
        addMember(id, staffId, "MEMBER");
      } else if (existing.getLeftAt() != null) {
        memberMapper.update(null, Wrappers.lambdaUpdate(ChatMember.class).set(ChatMember::getLeftAt, null)
            .set(ChatMember::getJoinedAt, LocalDateTime.now()).eq(ChatMember::getId, existing.getId()));
      }
    }
    for (Long staffId : request.remove() == null ? List.<Long>of() : request.remove()) {
      ChatMember target = memberMapper.selectOne(Wrappers.lambdaQuery(ChatMember.class)
          .eq(ChatMember::getConversationId, id).eq(ChatMember::getStaffId, staffId).isNull(ChatMember::getLeftAt));
      if (target == null) {
        continue;
      }
      if ("OWNER".equals(target.getMemberRole())) {
        throw new IllegalArgumentException("不能移出群主");
      }
      target.setLeftAt(LocalDateTime.now());
      memberMapper.updateById(target);
    }
    postSystem(conv, me.getStaffId(), "群成员已更新");
    return Result.ok(null);
  }

  // ---------- 消息 ----------

  /** 游标分页：beforeId 之前的 limit 条，按时间正序返回；任务卡附带任务当前状态 */
  @GetMapping("/conversations/{id}/messages")
  public Result<List<Map<String, Object>>> messages(@PathVariable Long id,
      @RequestParam(required = false) Long beforeId, @RequestParam(required = false) Long afterId,
      @RequestParam(defaultValue = "30") int limit) {
    requireMember(id);
    int size = Math.max(1, Math.min(limit, 100));
    List<ChatMessage> list = messageMapper.selectList(Wrappers.lambdaQuery(ChatMessage.class)
        .eq(ChatMessage::getConversationId, id)
        .lt(beforeId != null, ChatMessage::getId, beforeId)
        .gt(afterId != null, ChatMessage::getId, afterId)
        .orderBy(true, afterId != null, ChatMessage::getId)
        .last("LIMIT " + size));
    if (afterId == null) {
      java.util.Collections.reverse(list);
    }
    Map<Long, OaTask> tasks = taskMap(list.stream().map(ChatMessage::getTaskId).filter(Objects::nonNull).toList());
    Map<Long, Long> linkCounts = linkMapper.selectList(Wrappers.lambdaQuery(ChatTaskLink.class)
            .in(!list.isEmpty(), ChatTaskLink::getMessageId, list.stream().map(ChatMessage::getId).toList())
            .eq(list.isEmpty(), ChatTaskLink::getId, -1L))
        .stream().collect(Collectors.groupingBy(ChatTaskLink::getMessageId, Collectors.counting()));
    return Result.ok(list.stream().map(m -> toMessage(m, tasks.get(m.getTaskId()), linkCounts.getOrDefault(m.getId(), 0L))).toList());
  }

  public record SendRequest(String body, String clientMessageId, Long replyToId) {}

  /** 发送：clientMessageId 保证超时重试不重复；返回服务端消息（含 ID） */
  @PostMapping("/conversations/{id}/messages")
  @Transactional
  public Result<Map<String, Object>> send(@PathVariable Long id, @RequestBody SendRequest request) {
    ChatMember me = requireMember(id);
    String body = request.body() == null ? "" : request.body().trim();
    if (body.isEmpty()) {
      throw new IllegalArgumentException("消息不能为空");
    }
    if (body.length() > MAX_BODY) {
      throw new IllegalArgumentException("消息不能超过 " + MAX_BODY + " 字");
    }
    String clientId = request.clientMessageId() == null || request.clientMessageId().isBlank() ? null : request.clientMessageId().trim();
    if (clientId != null) {
      ChatMessage duplicate = messageMapper.selectOne(Wrappers.lambdaQuery(ChatMessage.class)
          .eq(ChatMessage::getSenderId, me.getStaffId()).eq(ChatMessage::getClientMessageId, clientId).last("LIMIT 1"));
      if (duplicate != null) {
        return Result.ok(toMessage(duplicate, null, 0));
      }
    }
    if (request.replyToId() != null) {
      ChatMessage quoted = messageMapper.selectById(request.replyToId());
      if (quoted == null || !Objects.equals(quoted.getConversationId(), id)) {
        throw new IllegalArgumentException("引用的消息不在本会话");
      }
    }
    ChatConversation conv = conversationMapper.selectById(id);
    ChatMessage message = new ChatMessage();
    message.setConversationId(id);
    message.setSenderId(me.getStaffId());
    message.setSenderName(myName());
    message.setMessageType("TEXT");
    message.setBody(body);
    message.setReplyToId(request.replyToId());
    message.setClientMessageId(clientId);
    message.setCreateTime(LocalDateTime.now());
    try {
      messageMapper.insert(message);
    } catch (DuplicateKeyException race) {
      return Result.ok(toMessage(messageMapper.selectOne(Wrappers.lambdaQuery(ChatMessage.class)
          .eq(ChatMessage::getSenderId, me.getStaffId()).eq(ChatMessage::getClientMessageId, clientId).last("LIMIT 1")), null, 0));
    }
    afterAppend(conv, message, me.getStaffId());
    return Result.ok(toMessage(message, null, 0));
  }

  public record ReadRequest(Long messageId) {}

  /** 已读游标只前进，且不超过会话里实际存在的最新消息 */
  @PostMapping("/conversations/{id}/read")
  public Result<Void> read(@PathVariable Long id, @RequestBody ReadRequest request) {
    ChatMember me = requireMember(id);
    ChatMessage target = request.messageId() == null ? null : messageMapper.selectById(request.messageId());
    if (target == null || !Objects.equals(target.getConversationId(), id)) {
      throw new IllegalArgumentException("消息不在本会话");
    }
    if (me.getLastReadMessageId() == null || target.getId() > me.getLastReadMessageId()) {
      me.setLastReadMessageId(target.getId());
      memberMapper.updateById(me);
    }
    return Result.ok(null);
  }

  /** 发送人 2 分钟内可撤回；撤回保留记录但不再显示原文 */
  @PostMapping("/messages/{messageId}/recall")
  public Result<Void> recall(@PathVariable Long messageId) {
    ChatMessage message = messageMapper.selectById(messageId);
    if (message == null) {
      throw new IllegalArgumentException("消息不存在");
    }
    requireMember(message.getConversationId());
    if (!Objects.equals(message.getSenderId(), AuthContext.getStaffId())) {
      throw new IllegalArgumentException("只能撤回自己发送的消息");
    }
    if (message.getCreateTime() == null || message.getCreateTime().plus(RECALL_WINDOW).isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("超过 2 分钟的消息不能撤回");
    }
    message.setRecalledAt(LocalDateTime.now());
    messageMapper.updateById(message);
    notifyMembers(message.getConversationId(), Map.of("eventType", "CHAT_MESSAGE_RECALLED",
        "conversationId", String.valueOf(message.getConversationId()), "messageId", String.valueOf(messageId)));
    return Result.ok(null);
  }

  /** 只搜索我当前所在会话的消息 */
  @GetMapping("/search")
  public Result<List<Map<String, Object>>> search(@RequestParam String q) {
    String keyword = q == null ? "" : q.trim();
    if (keyword.length() < 2) {
      throw new IllegalArgumentException("请至少输入 2 个字");
    }
    List<Long> convIds = memberMapper.selectList(Wrappers.lambdaQuery(ChatMember.class)
        .eq(ChatMember::getStaffId, AuthContext.getStaffId()).isNull(ChatMember::getLeftAt))
        .stream().map(ChatMember::getConversationId).toList();
    if (convIds.isEmpty()) {
      return Result.ok(List.of());
    }
    return Result.ok(messageMapper.selectList(Wrappers.lambdaQuery(ChatMessage.class)
            .in(ChatMessage::getConversationId, convIds).isNull(ChatMessage::getRecalledAt)
            .like(ChatMessage::getBody, keyword).orderByDesc(ChatMessage::getId).last("LIMIT 30"))
        .stream().map(m -> toMessage(m, null, 0)).toList());
  }

  // ---------- 消息转待办 ----------

  public record ConvertRequest(String title, Long assigneeId, LocalDateTime endTime, String priority,
      String description, String planCategory, Boolean confirmDuplicate) {}

  /**
   * 引用一条消息创建统一任务（oa_task）。同一消息已有任务时默认拒绝并返回已有任务，
   * 需要明确确认才再建一张；任务卡写回会话，办理人不因此加入会话。
   */
  @PostMapping("/messages/{messageId}/convert-to-task")
  @Transactional
  public Result<Map<String, Object>> convertToTask(@PathVariable Long messageId, @RequestBody ConvertRequest request) {
    ChatMessage source = messageMapper.selectById(messageId);
    if (source == null || source.getRecalledAt() != null) {
      throw new IllegalArgumentException("来源消息不存在或已撤回");
    }
    ChatMember me = requireMember(source.getConversationId());
    String title = request.title() == null ? "" : request.title().trim();
    if (title.isEmpty()) {
      throw new IllegalArgumentException("请填写任务标题");
    }
    ChatConversation conv = conversationMapper.selectById(source.getConversationId());
    StaffAccount assignee = request.assigneeId() == null ? null : staffMapper.selectById(request.assigneeId());
    if (assignee == null || !Objects.equals(assignee.getOrgId(), conv.getOrgId()) || !Integer.valueOf(1).equals(assignee.getStatus())) {
      throw new IllegalArgumentException("负责人必须是本机构在职员工");
    }
    List<ChatTaskLink> existing = linkMapper.selectList(Wrappers.lambdaQuery(ChatTaskLink.class).eq(ChatTaskLink::getMessageId, messageId));
    if (!existing.isEmpty() && !Boolean.TRUE.equals(request.confirmDuplicate())) {
      Map<String, Object> dup = new LinkedHashMap<>();
      dup.put("duplicate", true);
      dup.put("existingTaskIds", existing.stream().map(l -> String.valueOf(l.getTaskId())).toList());
      return Result.ok(dup);
    }
    OaTask task = new OaTask();
    task.setTenantId(conv.getOrgId());
    task.setOrgId(conv.getOrgId());
    task.setTitle(title);
    task.setDescription(((request.description() == null ? "" : request.description().trim() + "\n")
        + "来源：" + (conv.getTitle() == null ? "单聊" : conv.getTitle()) + " · " + source.getSenderName() + "：" + abbreviate(source.getBody(), 200)).trim());
    task.setStartTime(LocalDateTime.now());
    task.setEndTime(request.endTime());
    task.setPriority(List.of("HIGH", "NORMAL", "LOW").contains(String.valueOf(request.priority()).toUpperCase())
        ? request.priority().toUpperCase() : "NORMAL");
    task.setStatus("OPEN");
    task.setAssigneeId(assignee.getId());
    task.setAssigneeName(assignee.getRealName());
    task.setCalendarType("COLLAB");
    task.setPlanCategory(request.planCategory() == null || request.planCategory().isBlank() ? "聊天待办" : request.planCategory().trim());
    task.setUrgency("NORMAL");
    task.setIsRecurring(0);
    task.setCreatedBy(me.getStaffId());
    taskMapper.insert(task);

    ChatTaskLink link = new ChatTaskLink();
    link.setOrgId(conv.getOrgId());
    link.setTaskId(task.getId());
    link.setConversationId(conv.getId());
    link.setMessageId(messageId);
    link.setCreatedBy(me.getStaffId());
    link.setCreateTime(LocalDateTime.now());
    linkMapper.insert(link);

    ChatMessage card = new ChatMessage();
    card.setConversationId(conv.getId());
    card.setSenderId(me.getStaffId());
    card.setSenderName(myName());
    card.setMessageType("TASK_CARD");
    card.setBody("已转为待办：" + title + "（负责人 " + assignee.getRealName() + "）");
    card.setReplyToId(messageId);
    card.setTaskId(task.getId());
    card.setCreateTime(LocalDateTime.now());
    messageMapper.insert(card);
    afterAppend(conv, card, me.getStaffId());
    socket.sendToStaff(conv.getOrgId(), assignee.getId(), Map.of("eventType", "CHAT_TASK_ASSIGNED",
        "taskId", String.valueOf(task.getId()), "title", title));

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("duplicate", false);
    result.put("taskId", String.valueOf(task.getId()));
    result.put("message", toMessage(card, task, 0));
    return Result.ok(result);
  }

  /**
   * 聊天待办：来源为聊天的统一任务。view=mine 我负责 / created 我发起 / involved 我所在会话产生的；
   * 只返回与我有关的任务，状态以 oa_task 为准。
   */
  @GetMapping("/tasks")
  public Result<List<Map<String, Object>>> tasks(@RequestParam(defaultValue = "all") String view,
      @RequestParam(required = false) String status) {
    Long me = AuthContext.getStaffId();
    Long orgId = AuthContext.getOrgId();
    List<ChatTaskLink> links = linkMapper.selectList(Wrappers.lambdaQuery(ChatTaskLink.class)
        .eq(ChatTaskLink::getOrgId, orgId).orderByDesc(ChatTaskLink::getId).last("LIMIT 500"));
    if (links.isEmpty()) {
      return Result.ok(List.of());
    }
    Set<Long> myConvs = memberMapper.selectList(Wrappers.lambdaQuery(ChatMember.class)
            .eq(ChatMember::getStaffId, me).isNull(ChatMember::getLeftAt))
        .stream().map(ChatMember::getConversationId).collect(Collectors.toSet());
    Map<Long, OaTask> tasks = taskMap(links.stream().map(ChatTaskLink::getTaskId).distinct().toList());
    Map<Long, ChatConversation> convs = conversationMapper.selectBatchIds(links.stream().map(ChatTaskLink::getConversationId).distinct().toList())
        .stream().collect(Collectors.toMap(ChatConversation::getId, Function.identity()));
    List<Map<String, Object>> rows = new ArrayList<>();
    Set<Long> seen = new LinkedHashSet<>();
    for (ChatTaskLink link : links) {
      OaTask task = tasks.get(link.getTaskId());
      if (task == null || !seen.add(task.getId())) {
        continue;
      }
      boolean mine = Objects.equals(task.getAssigneeId(), me);
      boolean created = Objects.equals(task.getCreatedBy(), me);
      boolean involved = myConvs.contains(link.getConversationId());
      boolean visible = switch (view) {
        case "mine" -> mine;
        case "created" -> created;
        case "involved" -> involved;
        default -> mine || created || involved;
      };
      if (!visible || (status != null && !status.isBlank() && !status.equalsIgnoreCase(task.getStatus()))) {
        continue;
      }
      ChatConversation conv = convs.get(link.getConversationId());
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("taskId", String.valueOf(task.getId()));
      row.put("title", task.getTitle());
      row.put("status", task.getStatus());
      row.put("priority", task.getPriority());
      row.put("assigneeName", task.getAssigneeName());
      row.put("planCategory", task.getPlanCategory());
      row.put("endTime", task.getEndTime());
      row.put("updateTime", task.getUpdateTime());
      row.put("mine", mine);
      row.put("created", created);
      // 不在来源会话里的人看不到会话标题与入口，避免借任务读取群聊
      row.put("conversationId", involved ? String.valueOf(link.getConversationId()) : null);
      row.put("conversationTitle", involved && conv != null ? (conv.getTitle() == null ? "单聊" : conv.getTitle()) : "（无权查看来源会话）");
      row.put("messageId", involved ? String.valueOf(link.getMessageId()) : null);
      rows.add(row);
    }
    return Result.ok(rows);
  }

  // ---------- 内部 ----------

  private ChatMember requireMember(Long conversationId) {
    ChatMember member = memberMapper.selectOne(Wrappers.lambdaQuery(ChatMember.class)
        .eq(ChatMember::getConversationId, conversationId).eq(ChatMember::getStaffId, AuthContext.getStaffId())
        .isNull(ChatMember::getLeftAt).last("LIMIT 1"));
    if (member == null) {
      throw new org.springframework.security.access.AccessDeniedException("您不在该会话中");
    }
    return member;
  }

  private List<ChatMember> activeMembers(Long conversationId) {
    return memberMapper.selectList(Wrappers.lambdaQuery(ChatMember.class)
        .eq(ChatMember::getConversationId, conversationId).isNull(ChatMember::getLeftAt));
  }

  private List<StaffAccount> activeStaff(Long orgId, Collection<Long> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0).eq(StaffAccount::getStatus, 1)
        .eq(orgId != null, StaffAccount::getOrgId, orgId).in(StaffAccount::getId, ids));
  }

  private Map<Long, StaffAccount> staffMap(List<Long> ids) {
    return ids.isEmpty() ? new java.util.HashMap<>() : staffMapper.selectBatchIdsSafe(ids).stream()
        .collect(Collectors.toMap(StaffAccount::getId, Function.identity(), (a, b) -> a));
  }

  private Map<Long, OaTask> taskMap(List<Long> ids) {
    return ids.isEmpty() ? new java.util.HashMap<>() : taskMapper.selectBatchIds(ids).stream()
        .filter(t -> !Integer.valueOf(1).equals(t.getIsDeleted()))
        .collect(Collectors.toMap(OaTask::getId, Function.identity()));
  }

  private Map<Long, String> directPeerNames(List<ChatConversation> convs, Long me) {
    List<Long> directIds = convs.stream().filter(c -> "DIRECT".equals(c.getConvType())).map(ChatConversation::getId).toList();
    if (directIds.isEmpty()) {
      return new java.util.HashMap<>();
    }
    List<ChatMember> peers = memberMapper.selectList(Wrappers.lambdaQuery(ChatMember.class)
        .in(ChatMember::getConversationId, directIds).ne(ChatMember::getStaffId, me));
    Map<Long, StaffAccount> staff = staffMap(peers.stream().map(ChatMember::getStaffId).toList());
    return peers.stream().collect(Collectors.toMap(ChatMember::getConversationId,
        p -> staff.containsKey(p.getStaffId()) ? staff.get(p.getStaffId()).getRealName() : "已离职", (a, b) -> a));
  }

  private ChatConversation newConversation(Long orgId, String type, String title, Long me) {
    ChatConversation conv = new ChatConversation();
    conv.setOrgId(orgId);
    conv.setConvType(type);
    conv.setTitle(title);
    conv.setCreatedBy(me);
    conv.setCreateTime(LocalDateTime.now());
    conv.setLastMessageAt(LocalDateTime.now());
    return conv;
  }

  private void addMember(Long conversationId, Long staffId, String role) {
    ChatMember member = new ChatMember();
    member.setConversationId(conversationId);
    member.setStaffId(staffId);
    member.setMemberRole(role);
    member.setJoinedAt(LocalDateTime.now());
    member.setMuted(0);
    memberMapper.insert(member);
  }

  private void postSystem(ChatConversation conv, Long actor, String text) {
    ChatMessage message = new ChatMessage();
    message.setConversationId(conv.getId());
    message.setSenderId(actor);
    message.setSenderName("系统");
    message.setMessageType("SYSTEM");
    message.setBody(text);
    message.setCreateTime(LocalDateTime.now());
    messageMapper.insert(message);
    afterAppend(conv, message, actor);
  }

  /** 更新会话摘要、发送者已读游标，并推送给其他在群成员（推送失败不影响已保存的消息） */
  private void afterAppend(ChatConversation conv, ChatMessage message, Long senderId) {
    conv.setLastMessageId(message.getId());
    conv.setLastMessagePreview(abbreviate(message.getBody(), 60));
    conv.setLastMessageAt(message.getCreateTime());
    conversationMapper.updateById(conv);
    memberMapper.update(null, Wrappers.lambdaUpdate(ChatMember.class).set(ChatMember::getLastReadMessageId, message.getId())
        .eq(ChatMember::getConversationId, conv.getId()).eq(ChatMember::getStaffId, senderId));
    notifyMembers(conv.getId(), Map.of("eventType", "CHAT_MESSAGE",
        "conversationId", String.valueOf(conv.getId()), "messageId", String.valueOf(message.getId())));
  }

  private void notifyMembers(Long conversationId, Map<String, Object> payload) {
    Long orgId = AuthContext.getOrgId();
    for (ChatMember member : activeMembers(conversationId)) {
      try {
        socket.sendToStaff(orgId, member.getStaffId(), payload);
      } catch (RuntimeException ignored) {
        // 实时推送只是提醒，客户端会按游标补拉
      }
    }
  }

  private Map<String, Object> toMessage(ChatMessage m, OaTask task, long linkCount) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", String.valueOf(m.getId()));
    row.put("conversationId", String.valueOf(m.getConversationId()));
    row.put("senderId", String.valueOf(m.getSenderId()));
    row.put("senderName", m.getSenderName());
    row.put("type", m.getMessageType());
    row.put("body", m.getRecalledAt() == null ? m.getBody() : null);
    row.put("recalled", m.getRecalledAt() != null);
    row.put("replyToId", m.getReplyToId() == null ? null : String.valueOf(m.getReplyToId()));
    row.put("clientMessageId", m.getClientMessageId());
    row.put("createTime", m.getCreateTime());
    row.put("linkedTaskCount", linkCount);
    if (m.getTaskId() != null) {
      Map<String, Object> card = new LinkedHashMap<>();
      card.put("taskId", String.valueOf(m.getTaskId()));
      card.put("title", task == null ? null : task.getTitle());
      card.put("status", task == null ? "UNKNOWN" : task.getStatus());
      card.put("assigneeName", task == null ? null : task.getAssigneeName());
      card.put("endTime", task == null ? null : task.getEndTime());
      row.put("task", card);
    }
    return row;
  }

  private String myName() {
    StaffAccount me = staffMapper.selectById(AuthContext.getStaffId());
    return me == null ? AuthContext.getUsername() : me.getRealName();
  }

  private static String abbreviate(String text, int max) {
    if (text == null) {
      return "";
    }
    return text.length() <= max ? text : text.substring(0, max) + "…";
  }
}
