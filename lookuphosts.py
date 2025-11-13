class HostResolver:
    def __init__(self, json_path="jsonhosts"):
        self.host_map = self._load_host_map(json_path)

    def get_hosts(self, envs, site, comp):
        """
        envs: list of environments (e.g. ["UAT01","UAT02"])
        site: "RCC", "WSDC", or "ALL"/"BOTH"
        comp: "STP", "WB", or "ALL/BOTH"
        Returns: CSV string of hostnames
        """
    def get_hosts(self, envs, site, comp):
        # If envs is a string, split on commas
        if isinstance(envs, str):
            envs = [e.strip() for e in envs.split(",")]
        hosts = []
        for env in envs:
            env_map = self.host_map.get(env, {})
            sites_to_use = env_map.keys() if site in ("ALL") else [site]
            for s in sites_to_use:
                comp_map = env_map.get(s, {})
                comps_to_use = ["STP","WB"] if comp == "ALL" else [comp]
                for c in comps_to_use:
                    hosts.extend(comp_map.get(c, []))
        return ",".join(sorted(set(hosts)))
