import ansible_runner, os

r = ansible_runner.run(
    private_data_dir='.',  # run directly from repo
    playbook='playbooks/list.yml',
    inventory='inventories/ansible_hosts',
    extravars={'fetch_dest': '/tmp/fetched/'},
    envvars={
        **os.environ,  # inherit everything from Jenkins environment
        'ANSIBLE_HOST_KEY_CHECKING': 'False',
    },
    quiet=False
)

print("Status:", r.status)
print("RC:", r.rc)
print("Stdout:", r.stdout.read())

