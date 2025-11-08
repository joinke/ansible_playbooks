#!/usr/bin/env python3
import os
import subprocess
import sys

WORKSPACE = os.getenv("WORKSPACE", "/tmp")
SSH_KEY = os.getenv("SSH_KEY")
SSH_USER = os.getenv("SSH_USER")

# Construct Ansible command
cmd = [
    "ansible-playbook",
    "-i", "inventories/ansible_hosts",
    "playbooks/counter.yml",
    "--become",
    "--become-user", "wanpen",
    "--extra-vars", f"fetch_dest={WORKSPACE}/fetched/",
    "-u", SSH_USER,             # use Jenkins-provided username
    "--private-key", SSH_KEY,   # use Jenkins-provided key file
    "-v"
]

env = os.environ.copy()
env["ANSIBLE_HOST_KEY_CHECKING"] = "False"

print("✅ Running command:\n", " ".join(cmd), "\n", flush=True)

process = subprocess.Popen(cmd, env=env, stdout=sys.stdout, stderr=sys.stderr)
rc = process.wait()

print(f"\n🔹 Ansible playbook finished with exit code: {rc}")
sys.exit(rc)


