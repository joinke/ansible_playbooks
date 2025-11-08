#!/usr/bin/env python3
import os
import subprocess
import sys

# Environment variables
user = os.getenv("SSH_USER")
password = os.getenv("SSH_PASS")
WORKSPACE = os.getenv("WORKSPACE", "/tmp")

cmd = [
    "ansible-playbook",
    "-i", "inventories/ansible_hosts",
    "playbooks/list.yml",
    "--become",
    "--become-user", "wanpen",
    "--extra-vars", f"fetch_dest={WORKSPACE}/fetched/",
    "-u", user,
    "-v"
]

# Copy environment and add Ansible settings
env = os.environ.copy()
env["ANSIBLE_HOST_KEY_CHECKING"] = "False"
env["ANSIBLE_ASK_PASS"] = "True"
env["ANSIBLE_PASSWORD"] = password  # if modules read it

# Force Python stdout/stderr to be unbuffered
sys.stdout.reconfigure(line_buffering=True)
sys.stderr.reconfigure(line_buffering=True)

# Launch subprocess with real-time output
process = subprocess.Popen(
    cmd,
    env=env,
    stdout=sys.stdout,
    stderr=sys.stderr
)

# Wait for completion and exit with the same code
sys.exit(process.wait())
