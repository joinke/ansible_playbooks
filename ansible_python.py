#!/usr/bin/env python3
import os
import subprocess
import sys

user = os.getenv("SSH_USER")
password = os.getenv("SSH_PASS")
WORKSPACE = os.getenv("WORKSPACE", "/tmp")

cmd = [
    "ansible-playbook",
    "-i", "inventories/ansible_hosts",
    "playbooks/counter.yml",
    "--become",
    "--become-user", "wanpen",
    "--extra-vars", f"fetch_dest={WORKSPACE}/fetched/",
    "-u", user,
    "-v"
]

env = os.environ.copy()
env["ANSIBLE_HOST_KEY_CHECKING"] = "False"
env["ANSIBLE_STDOUT_CALLBACK"] = "minimal"

# Use subprocess with stdout=PIPE and line buffering
with subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=env, bufsize=1, text=True) as process:
    for line in process.stdout:
        print(line, end='', flush=True)  # print line immediately
    process.wait()
    sys.exit(process.returncode)
