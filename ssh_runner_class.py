#!/usr/bin/env python3
import paramiko
import os

class SSHRunner:
    def __init__(self, inventory_file, key_file=None):
        self.hosts = []
        self.user = os.getenv("SSH_USER")        # SSH user from environment
        self.password = os.getenv("SSH_PASS")    # SSH password from environment
        self.key_file = os.getenv("SSH_PASS")               # Optional SSH key file
        self._load_inventory(inventory_file)

    def _load_inventory(self, inventory_file):
        """Load hosts from a file (simple one-host-per-line format)"""
        with open(inventory_file) as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#"):
                    self.hosts.append(line)

    def run_command(self, command_args):
        """
        Run a command with arguments on all hosts.
        command_args: list of command parts, e.g. ['python3', '/tmp/example.py', '--arg', 'value']
        """
        if not isinstance(command_args, list):
            raise TypeError("command_args must be a list")

        for host in self.hosts:
            print(f"\n🟢 Connecting to {host} as {self.user}...")
            client = paramiko.SSHClient()
            client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            try:
                client.connect(
                    hostname=host,
                    username=self.user,
                    key_filename=self.key_file,
                    #password=self.password,
                    look_for_keys=False,
                    allow_agent=True
                )
                
                # Join command args safely into a shell command
                command = " ".join(paramiko.util.escape_shell(arg) for arg in command_args)

                stdin, stdout, stderr = client.exec_command(command)
                
                # Stream stdout in real-time
                for line in iter(stdout.readline, ""):
                    print(f"[{host}] {line}", end='')

                # Stream stderr in real-time
                for line in iter(stderr.readline, ""):
                    print(f"[{host}][ERR] {line}", end='')

            except Exception as e:
                print(f"[{host}][ERROR] {e}")
            finally:
                client.close()
