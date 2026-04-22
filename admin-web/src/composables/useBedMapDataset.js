import { readonly, ref } from 'vue';
import { getBaseConfigItemList } from '../api/baseConfig';
import { getBedMap, getRoomList } from '../api/bed';
const plainBeds = ref([]);
const riskBeds = ref([]);
const roomList = ref([]);
const roomTypeItems = ref([]);
const bedTypeItems = ref([]);
const areaItems = ref([]);
let plainBedPromise = null;
let riskBedPromise = null;
let roomListPromise = null;
let configPromise = null;
async function ensureBedMapLoaded(includeRisk = false, force = false) {
    if (includeRisk) {
        if (!force && riskBeds.value.length)
            return riskBeds.value;
        if (!force && riskBedPromise)
            return riskBedPromise;
        riskBedPromise = getBedMap({ params: { includeRisk: true } })
            .then((data) => {
            riskBeds.value = data || [];
            plainBeds.value = data || [];
            return riskBeds.value;
        })
            .finally(() => {
            riskBedPromise = null;
        });
        return riskBedPromise;
    }
    if (!force && plainBeds.value.length)
        return plainBeds.value;
    if (!force && plainBedPromise)
        return plainBedPromise;
    plainBedPromise = getBedMap()
        .then((data) => {
        plainBeds.value = data || [];
        return plainBeds.value;
    })
        .finally(() => {
        plainBedPromise = null;
    });
    return plainBedPromise;
}
async function ensureRoomListLoaded(force = false) {
    if (!force && roomList.value.length)
        return roomList.value;
    if (!force && roomListPromise)
        return roomListPromise;
    roomListPromise = getRoomList()
        .then((data) => {
        roomList.value = data || [];
        return roomList.value;
    })
        .finally(() => {
        roomListPromise = null;
    });
    return roomListPromise;
}
async function ensureResidenceConfigLoaded(force = false) {
    if (!force && roomTypeItems.value.length && bedTypeItems.value.length && areaItems.value.length)
        return;
    if (!force && configPromise)
        return configPromise;
    configPromise = Promise.all([
        getBaseConfigItemList({ configGroup: 'ADMISSION_ROOM_TYPE', status: 1 }),
        getBaseConfigItemList({ configGroup: 'ADMISSION_BED_TYPE', status: 1 }),
        getBaseConfigItemList({ configGroup: 'ADMISSION_AREA', status: 1 })
    ])
        .then(([roomTypes, bedTypes, areas]) => {
        roomTypeItems.value = roomTypes || [];
        bedTypeItems.value = bedTypes || [];
        areaItems.value = areas || [];
    })
        .finally(() => {
        configPromise = null;
    });
    return configPromise;
}
async function refreshBedMapDataset(options) {
    const tasks = [];
    tasks.push(ensureBedMapLoaded(Boolean(options?.includeRisk), true));
    if (options?.rooms)
        tasks.push(ensureRoomListLoaded(true));
    if (options?.residenceConfig)
        tasks.push(ensureResidenceConfigLoaded(true));
    await Promise.all(tasks);
}
export function useBedMapDataset() {
    return {
        plainBeds: readonly(plainBeds),
        riskBeds: readonly(riskBeds),
        roomList: readonly(roomList),
        roomTypeItems: readonly(roomTypeItems),
        bedTypeItems: readonly(bedTypeItems),
        areaItems: readonly(areaItems),
        ensureBedMapLoaded,
        ensureRoomListLoaded,
        ensureResidenceConfigLoaded,
        refreshBedMapDataset
    };
}
