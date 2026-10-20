# -*- coding: utf-8 -*-
"""M3 互动功能端到端验收：评论 / 点赞 / 阅读量"""
import json
import time
import urllib.request
import urllib.error
import subprocess
import sys

BASE = "http://localhost:8081/api"
MYSQL = r"C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe"
STAMP = str(int(time.time()))[-6:]

passed = 0
failed = 0
failures = []


def check(name, cond, detail=""):
    global passed, failed
    if cond:
        passed += 1
        print(f"  PASS  {name}")
    else:
        failed += 1
        failures.append(name)
        print(f"  FAIL  {name}  {detail}")


def req(method, path, body=None, token=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        r.add_header("Authorization", token)
    try:
        with urllib.request.urlopen(r, timeout=10) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "ignore")
        try:
            return json.loads(raw)
        except Exception:
            return {"code": e.code, "message": raw}
    except Exception as e:
        return {"code": -1, "message": str(e)}


def sql(q):
    out = subprocess.run([MYSQL, "-uroot", "-p11111111", "-D", "blog", "-N", "-e", q],
                         capture_output=True, text=True)
    return out.stdout.strip()


def register_login(username, email, password="123456", role=None):
    req("POST", "/auth/register", {"username": username, "email": email, "password": password})
    if role:
        sql(f"update user set role='{role}' where username='{username}'")
    res = req("POST", "/auth/login", {"username": username, "password": password})
    assert res.get("code") == 0, f"login {username} failed: {res}"
    return res["data"]["token"], res["data"]["userInfo"]


print("=" * 64)
print("M3 互动功能验收开始，时间戳后缀:", STAMP)
print("=" * 64)

u1 = "m3u1" + STAMP
u2 = "m3u2" + STAMP
u3 = "m3u3" + STAMP
u4 = "m3u4" + STAMP
adm = "m3a" + STAMP
t1, info1 = register_login(u1, f"{u1}@test.com")
t2, info2 = register_login(u2, f"{u2}@test.com")
t3, _ = register_login(u3, f"{u3}@test.com")
t4, _ = register_login(u4, f"{u4}@test.com")
ta, _ = register_login(adm, f"{adm}@test.com", role="ADMIN")
nick1 = info1.get("nickname") or u1
nick2 = info2.get("nickname") or u2

# 选一篇已发布文章
page = req("GET", "/article/page?page=1&size=5")
arts = page["data"]["records"]
pub_ids = [a["id"] for a in arts]
AID = 5 if 5 in pub_ids else pub_ids[0]
AID2 = 3 if 3 in pub_ids and 3 != AID else (pub_ids[1] if len(pub_ids) > 1 else AID)
print(f"使用已发布文章 id={AID}（限流测试用 id={AID2}）\n")

# 清掉该文章旧评论，保证断言干净
sql(f"delete from comment where article_id in ({AID},{AID2})")

print("[1] 评论读取 / 登录校验")
r = req("GET", f"/comment/page?articleId={AID}")
check("游客可查看评论列表", r.get("code") == 0 and isinstance(r["data"].get("records"), list), r)
r = req("POST", "/comment", {"articleId": AID, "content": "游客评论"})
check("游客发评论被拒绝(401)", r.get("code") == 401, r)

print("\n[2] 顶层评论与分页")
r = req("POST", "/comment", {"articleId": AID, "content": "顶层评论 A"}, t1)
check("u1 发表顶层评论 A", r.get("code") == 0 and r.get("data"), r)
cid_a = r.get("data")
r = req("POST", "/comment", {"articleId": AID, "content": "顶层评论 B"}, t1)
check("u1 发表顶层评论 B", r.get("code") == 0 and r.get("data"), r)
cid_b = r.get("data")
r = req("GET", f"/comment/page?articleId={AID}&page=1&size=10")
recs = r["data"]["records"] if r.get("code") == 0 else []
check("列表返回 2 条顶层评论", len(recs) == 2, f"got {len(recs)}")
check("顶层评论按时间倒序（B 在前）", len(recs) == 2 and recs[0]["id"] == cid_b, recs)
r = req("GET", f"/comment/page?articleId={AID}&page=1&size=1")
check("分页 size=1 生效且 total=2", r.get("code") == 0 and len(r["data"]["records"]) == 1 and r["data"]["total"] == 2, r.get("data"))

print("\n[3] 楼中楼回复")
r = req("POST", "/comment", {"articleId": AID, "parentId": cid_a, "content": "u2 回复 A"}, t2)
check("u2 回复顶层评论 A", r.get("code") == 0, r)
rid = r.get("data")
r = req("GET", f"/comment/page?articleId={AID}&page=1&size=10")
a_vo = next((x for x in r["data"]["records"] if x["id"] == cid_a), None)
check("回复挂在 A.children 下", a_vo and len(a_vo.get("children") or []) == 1, a_vo)
check("回复展示被回复人昵称", a_vo and a_vo["children"][0].get("replyNickname") == nick1,
      a_vo["children"][0] if a_vo and a_vo.get("children") else None)
r = req("POST", "/comment", {"articleId": AID, "parentId": rid, "content": "u1 回复 u2 的回复"}, t1)
check("回复“回复”成功（三级存储在二级展示）", r.get("code") == 0, r)
r = req("GET", f"/comment/page?articleId={AID}&page=1&size=10")
a_vo = next((x for x in r["data"]["records"] if x["id"] == cid_a), None)
kids = a_vo.get("children") or [] if a_vo else []
check("A 下汇聚 2 条回复", len(kids) == 2, kids)
check("二级回复的 replyNickname 正确", len(kids) == 2 and kids[1].get("replyNickname") == nick2, kids)

print("\n[4] 评论删除权限与级联")
r = req("DELETE", f"/comment/{cid_a}", token=t2)
check("非本人删除被拒绝(403)", r.get("code") == 403, r)
r = req("DELETE", f"/comment/{cid_a}", token=t1)
check("本人删除成功", r.get("code") == 0, r)
r = req("GET", f"/comment/page?articleId={AID}&page=1&size=10")
all_ids = []
for x in r["data"]["records"]:
    all_ids.append(x["id"])
    all_ids.extend(c["id"] for c in (x.get("children") or []))
check("删除后 A 及其 2 条回复均不可见", cid_a not in all_ids and rid not in all_ids, all_ids)
r = req("DELETE", f"/comment/{cid_b}", token=ta)
check("管理员可删除他人评论", r.get("code") == 0, r)

print("\n[5] 参数校验与频率限制")
r = req("POST", "/comment", {"articleId": AID, "content": "   "}, t1)
check("空内容被拒绝(400)", r.get("code") == 400, r)
r = req("POST", "/comment", {"articleId": AID, "content": "x" * 1001}, t1)
check("超过 1000 字被拒绝(400)", r.get("code") == 400, r)
r = req("POST", "/comment", {"articleId": AID, "parentId": 999999999, "content": "回复不存在的评论"}, t1)
check("回复不存在的评论被拒绝(400)", r.get("code") == 400, r)
r = req("POST", "/comment", {"articleId": 6, "content": "评论未发布文章"}, t2)
check("未发布文章不能评论(404)", r.get("code") == 404, r)
sql(f"update user set status=0 where username='{u4}'")
r = req("POST", "/comment", {"articleId": AID, "content": "禁用用户评论"}, t4)
check("被禁用用户不能评论(403)", r.get("code") == 403, r)
codes = []
for i in range(6):
    rr = req("POST", "/comment", {"articleId": AID2, "content": f"限流测试 {i}"}, t3)
    codes.append(rr.get("code"))
check("每分钟前 5 条成功", codes[:5] == [0] * 5, codes)
check("第 6 条被限流(400)", codes[5] == 400, codes)

print("\n[6] 点赞")
r = req("POST", f"/like/article/{AID}")
check("游客点赞被拒绝(401)", r.get("code") == 401, r)
sql(f"delete from article_like where article_id={AID}")
r = req("POST", f"/like/article/{AID}", token=t2)
check("u2 点赞成功 liked=true count=1", r.get("code") == 0 and r["data"] == {"liked": True, "count": 1}, r)
check("article_like 唯一记录 1 条", sql(f"select count(*) from article_like where article_id={AID}") == "1")
r = req("GET", f"/like/article/{AID}/status", token=t2)
check("点赞状态接口 liked=true", r.get("code") == 0 and r["data"]["liked"] is True, r)
r = req("POST", f"/like/article/{AID}", token=t2)
check("再次点击取消点赞 count=0", r.get("code") == 0 and r["data"] == {"liked": False, "count": 0}, r)
r = req("POST", f"/like/article/{AID}", token=t2)
check("第三次点击恢复点赞", r.get("code") == 0 and r["data"]["liked"] is True and r["data"]["count"] == 1, r)
r = req("GET", f"/like/article/{AID}/count")
check("公开点赞数接口 = 1", r.get("code") == 0 and r["data"] == 1, r)
r = req("GET", f"/article/{AID}")
check("详情页 likeCount=1 且 liked 游客为 false", r.get("code") == 0 and r["data"]["likeCount"] == 1 and r["data"]["liked"] is False, r.get("data"))
r = req("POST", "/like/article/6", token=t2)
check("未发布文章不能点赞(404)", r.get("code") == 404, r)

print("\n[7] 阅读量（Redis 增量 + 定时落库）")
db_before = int(sql(f"select view_count from article where id={AID}") or 0)
redis_before = sql(f"select 1")  # noop
d1 = req("GET", f"/article/{AID}")
d2 = req("GET", f"/article/{AID}")
d3 = req("GET", f"/article/{AID}")
v_last = d3["data"]["viewCount"]
check("连续 3 次访问详情，展示值递增", d1["data"]["viewCount"] + 2 == v_last,
      (d1["data"]["viewCount"], d2["data"]["viewCount"], v_last))
check("DB 存量暂未变化（增量在 Redis）", int(sql(f"select view_count from article where id={AID}") or 0) == db_before, db_before)

print("\n[8] 等待定时任务落库（每 5 分钟，最多 6 分钟）…")
deadline = time.time() + 380
synced = False
while time.time() < deadline:
    now_db = int(sql(f"select view_count from article where id={AID}") or 0)
    if now_db > db_before:
        synced = True
        print(f"  定时落库生效：{db_before} -> {now_db}")
        break
    time.sleep(20)
check("阅读量定时回写 MySQL", synced, f"db_before={db_before}")

print("\n" + "=" * 64)
print(f"结果：PASS {passed} / FAIL {failed}")
if failures:
    print("失败项：")
    for f in failures:
        print("  -", f)
print("=" * 64)
sys.exit(1 if failed else 0)
