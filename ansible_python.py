#!/usr/bin/env python3
import os
import subprocess
import sys

user = os.getenv("SSH_USER")
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

# Use subprocess to stream output line by line
process = subprocess.Popen(cmd, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, bufsize=1)

# Stream to Jenkins console in real-time
for line in process.stdout:
    print(line, end='')  # print each line immediately

process.stdout.close()
sys.exit(process.wait())
