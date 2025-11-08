#!/usr/bin/env python3
import os
import subprocess

class SSHRunner:
    def __init__(self, hosts=None, user=None, key_file=None, inventory_file='hostsfile'):
        """
        hosts: optional list of host IPs / hostnames
        user: SSH username (optional, defaults to env SSH_USER)
        key_file: path to SSH private key (optional, defaults to env SSH_KEY)
        inventory_file: path to Ansible-style inventory file
        """
        self.user = user or os.getenv("SSH_USER")
        self.key_file = key_file or os.getenv("SSH_KEY")
        self.hosts = hosts or []

        if inventory_file:
            self.hosts = self._read_inventory(inventory_file)

        if not self.hosts:
            raise ValueError("No hosts provided or found in inventory file")

    def _read_inventory(self, filepath):
        """
        Simple parser for an Ansible hosts file.
        Only reads plain host entries (ignores groups and vars).
        """
        hosts = []
        with open(filepath) as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and not line.startswith("["):
                    # Remove inline comments
                    host = line.split()[0]
                    hosts.append(host)
        return hosts

    def run_command(self, command_args):
        """
        command_args: list of command and arguments, e.g. ["ls", "-l", "/tmp"]
        """
        if not command_args:
            raise ValueError("No command provided")

        for host in self.hosts:
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

            # Stream output in real time
            for line in iter(process.stdout.readline, ""):
                print(line, end="")

            retcode = process.wait()
            if retcode != 0:
                print(f"\n⚠️ Command failed on {host} with exit code {retcode}")
            else:
                print(f"\n✅ Command finished on {host}")
