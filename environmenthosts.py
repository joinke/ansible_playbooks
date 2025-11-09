import json
import os

class EnvironmentHosts:
    """
    Reads environment/component/host mappings from a JSON file and
    provides methods to retrieve host lists or a single host.
    """

    def __init__(self, json_path):
        if not os.path.exists(json_path):
            raise FileNotFoundError(f"Environment JSON file not found: {json_path}")
        with open(json_path, 'r') as f:
            self.env_data = json.load(f)

    def get_all_environments(self):
        """Return list of all environments."""
        return list(self.env_data.keys())

    def get_all_hosts(self, environment, component=None):
        """
        Return all hosts for an environment.
        If component is provided, return only hosts for that component.
        """
        env_key = self._find_env_key(environment)
        if not env_key:
            raise ValueError(f"Environment '{environment}' not found.")

        env_entry = self.env_data[env_key]
        if component:
            comp_key = self._find_component_key(env_entry, component)
            if not comp_key:
                raise ValueError(f"Component '{component}' not found in environment '{environment}'.")
            return env_entry[comp_key]
        else:
            # Return all hosts merged across components
            return [host for comp_hosts in env_entry.values() for host in comp_hosts]

    def get_first_host(self, environment, component=None):
        """Return the first host for a given environment (and optional component)."""
        hosts = self.get_all_hosts(environment, component)
        if hosts:
            return hosts[0]
        raise ValueError(f"No hosts found for environment '{environment}' and component '{component or 'ALL'}'.")

    # --- helpers ---
    def _find_env_key(self, env_name):
        """Case-insensitive environment lookup."""
        for key in self.env_data.keys():
            if key.lower() == env_name.lower():
                return key
        return None

    def _find_component_key(self, env_entry, component_name):
        """Case-insensitive component lookup."""
        for key in env_entry.keys():
            if key.lower() == component_name.lower():
                return key
        return None


# Example usage
if __name__ == "__main__":
    config_path = os.getenv("ENV_JSON", "environments.json")
    env_hosts = EnvironmentHosts(config_path)

    env = "UAT01"
    comp = "STP"

    print(f"Available environments: {env_hosts.get_all_environments()}")
    print(f"All hosts in {env}: {env_hosts.get_all_hosts(env)}")
    print(f"STP hosts in {env}: {env_hosts.get_all_hosts(env, comp)}")
    print(f"First host in {env}/{comp}: {env_hosts.get_first_host(env, comp)}")
