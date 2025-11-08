#!/usr/bin/env python3
import os
import subprocess
from concurrent.futures import ThreadPoolExecutor, as_completed

class SSHRunner:
    def __init__(self, hosts=None, user=None, key_file=None, max_workers=5):
        """
        hosts: list or comma-separated string of hostnames/IPs
        user: SSH user
        key_file: path to private key
        max_workers: parallelism
        """
        self.user = user or os.getenv("SSH_USER")
        self.key_file = key_file or os.getenv("SSH_KEY")
        self.max_workers = max_workers

        if isinstance(hosts, str):
            # Convert comma-separated string to list
            self.hosts = [h.strip() for h in hosts.split(",") if h.strip()]
        elif isinstance(hosts, list):
            self.hosts = hosts
        else:
            self.hosts = []

        if not self.hosts:
            raise ValueError("No hosts provided")

    def _run_on_host(self, host, command_args):
        ssh_cmd = ["ssh", "-o", "StrictHostKeyChecking=no"]
        if self.key_file:
            ssh_cmd.extend(["-i", self.key_file])
        remote = f"{self.user}@{host}" if self.user else host
        ssh_cmd.append(remote)
        ssh_cmd.extend(command_args)

        print(f"\n🖥 Running on {host}: {' '.join(command_args)}\n")
        process = subprocess.Popen(
            ssh_cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True
        )

        for line in iter(process.stdout.readline, ""):
            print(f"[{host}] {line}", end="")

        retcode = process.wait()
        if retcode != 0:
            print(f"\n⚠️ Command failed on {host} with exit code {retcode}")
        else:
            print(f"\n✅ Command finished on {host}")
        return retcode

    def run_command(self, command_args):
        with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            futures = {executor.submit(self._run_on_host, host, command_args): host for host in self.hosts}
            for future in as_completed(futures):
                host = futures[future]
                try:
                    future.result()
                except Exception as e:
                    print(f"\n❌ Exception on host {host}: {e}")
