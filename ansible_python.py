#!/usr/bin/env python3
import os
import subprocess
import sys

# Dynamic workspace path (Jenkins provides $WORKSPACE)
WORKSPACE = os.getenv("WORKSPACE", "/tmp")

# Construct ansible-playbook command
cmd = [
    "ansible-playbook",
    "-i", "inventories/ansible_hosts",
    "playbooks/list.yml",
    "--become",
    "--become-user", "wanpen",
    "--extra-vars", f"fetch_dest={WORKSPACE}/fetched/",
    "-v"
]

# Copy Jenkins environment (to keep SSH_AUTH_SOCK and others)
env = os.environ.copy()
env["ANSIBLE_HOST_KEY_CHECKING"] = "False"

print("✅ Running command:\n", " ".join(cmd), "\n", flush=True)

# Run Ansible and stream live output to Jenkins
process = subprocess.Popen(cmd, env=env, stdout=sys.stdout, stderr=sys.stderr)
rc = process.wait()

print(f"\n🔹 Ansible playbook finished with exit code: {rc}")
sys.exit(rc)


