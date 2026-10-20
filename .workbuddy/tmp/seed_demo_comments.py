# -*- coding: utf-8 -*-
"""给文章 5 造两条演示评论（顶层 + 楼中楼），用于界面截图验收"""
import json
import time
import urllib.request

BASE = "http://localhost:8081/api"
STAMP = str(int(time.time()))[-6:]
U1 = "demo" + STAMP
U2 = "demo2" + STAMP


def req(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    r = urllib.request.Request(BASE + path, data=data, method=method)
    r.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        r.add_header("Authorization", token)
    with urllib.request.urlopen(r) as resp:
        return json.loads(resp.read().decode())


def account(name):
    req("POST", "/auth/register", {"username": name, "email": f"{name}@test.com", "password": "123456"})
    return req("POST", "/auth/login", {"username": name, "password": "123456"})["data"]["token"]


t1 = account(U1)
t2 = account(U2)
root = req("POST", "/comment", {"articleId": 5, "content": "M3 界面验收：这条是顶层评论，楼中楼回复见下方 👍"}, t1)["data"]
req("POST", "/comment", {"articleId": 5, "parentId": root, "content": "楼中楼回复：评论、点赞、阅读量都能正常工作。"}, t2)
print("root id =", root)
