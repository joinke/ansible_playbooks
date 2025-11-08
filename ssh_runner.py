#!/usr/bin/env python3
import os
from ssh_runner_class import SSHRunner   # your SSHRunner class

WORKSPACE = os.getenv("WORKSPACE", "/tmp")

runner = SSHRunner(
    inventory_file="hostsfile",
    key_file=f"{WORKSPACE}/ssh_key.pem",  # optional, can be None if using password
    max_workers=10
)

# Run any command, e.g., your Python script with arguments
runner.run_command([
    "python3",
    "/tmp/example.py",
    "--fetch-dest",
    f"{WORKSPACE}/fetched/"
])
