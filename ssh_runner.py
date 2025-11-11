#!/usr/bin/env python3
import os
from ssh_runner_class import SSHRunner   # your SSHRunner class
from environmenthosts import EnvironmentHosts
from lookuphosts import HostResolver

WORKSPACE = os.getenv("WORKSPACE", "/tmp")
SELECTEDENVS = os.getenv("SELECTEDENVS","")
SELECTEDCOMP = os.getenv("SELECTEDCOMP","")
SELECTEDSITE = os.getenv("SELECTEDSITE","")

resolver = HostResolver()
HOSTS = resolver.get_hosts(SELECTEDENVS, SELECTEDSITE , SELECTEDCOMP)
runner = SSHRunner(
    hosts = HOSTS,
    max_workers=10
)
# Run any command, e.g., your Python script with arguments
print(f"My environments are {SELECTEDENVS} and components {SELECTEDCOMP}")
env_hosts = EnvironmentHosts("jsonhosts")
print(f"First host in {SELECTEDENVS}/{SELECTEDCOMP}: {env_hosts.get_first_host(SELECTEDENVS, SELECTEDCOMP)}")

runner.run_command([
    "python3",
    "/tmp/example.py",
    "--fetch-dest",
    f"{WORKSPACE}/fetched/"
])
