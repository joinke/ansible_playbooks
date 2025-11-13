#!/usr/bin/env python3
import os
from ssh_runner_class import SSHRunner   # your SSHRunner class
from lookuphosts import HostResolver

WORKSPACE = os.getenv("WORKSPACE", "/tmp")
SELECTEDOP = os.getenv("SELECTEDOPERATION","")
SELECTEDENVS = os.getenv("SELECTEDENVS","")
SELECTEDCOMP = os.getenv("SELECTEDCOMP","")
SELECTEDSITE = os.getenv("SELECTEDSITE","")
SELECTEDHOSTS = os.getenv("SELECTEDHOSTS","")

if SELECTEDHOSTS and SELECTEDHOSTS.strip():
    HOSTS = SELECTEDHOSTS
else:
    resolver = HostResolver()
    # CSV of required hosts
    HOSTS = resolver.get_hosts(SELECTEDENVS, SELECTEDSITE , SELECTEDCOMP)

runner = SSHRunner(
    hosts = HOSTS,
    max_workers=10
)
# Run any command, e.g., your Python script with arguments
print(f"My environments are {SELECTEDENVS} and components {SELECTEDCOMP} and  hosts {HOSTS} and site {SELECTEDSITE}")

runner.run_command([
    "python3",
    f"/tmp/{SELECTEDOP}",
    "--fetch-dest",
    f"/tmp/count_output.log"
])
