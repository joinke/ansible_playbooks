import ansible_runner, os

r = ansible_runner.run(
    private_data_dir='.',
    playbook='playbooks/list.yml',
    inventory='inventories/ansible_hosts',
    extravars={'fetch_dest': '/tmp/fetched/'},
    envvars={
        'ANSIBLE_HOST_KEY_CHECKING': 'False',
        'SSH_AUTH_SOCK': os.getenv('SSH_AUTH_SOCK')  # Jenkins-provided agent socket
    },
)
print("Status:", r.status)       # e.g. "successful"
print("RC:", r.rc)               # return code
print("Stdout:", r.stdout.read())  # full stdout text
