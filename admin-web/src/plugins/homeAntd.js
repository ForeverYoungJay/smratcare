import { Alert, Button, Card, Col, Collapse, DatePicker, Drawer, Empty, Form, Input, Modal, Row, Segmented, Select, Table, Tag } from 'ant-design-vue';
export function setupHomeAntd(app) {
    app.use(Alert);
    app.use(Button);
    app.use(Card);
    app.use(Col);
    app.use(Collapse);
    app.use(DatePicker);
    app.use(Drawer);
    app.use(Empty);
    app.use(Form);
    app.use(Input);
    app.use(Modal);
    app.use(Row);
    app.use(Segmented);
    app.use(Select);
    app.use(Table);
    app.use(Tag);
}
