import request from "./request"

export function getAllAccount() {
    return request.get("/allAccount");
}

export function getAllUser() {
    return request.get("/allUser");
}

export function modifyAccount(account) {
    return request.post("/modifyAccount",account);
}

export function addAccount(account) {
    return request.post("/addAccount",account);
}

export function deleteAccount(account) {
    return request.post("/deleteAccount",account);
}

export function getQuestionSummary(days) {
    return request.get("/getQuestionSummary?days="+days);
}

export function getAccountStatistics() {
    return request.get("/accountStatistics");
}

export function getUserUsageStatistics() {
    return request.get("/userUsage");
}

export function getRecord(cursor) {
    return request.get("/getRecord?cursor="+cursor);
}

export function getHistogramData() {
    return request.get("/getHistogramData");
}

export function getCaptcha() {
    return request.get("/getCaptcha");
}

export function login(form) {
    return request.post("/login",form);
}