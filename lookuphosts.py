class HostResolver:
    def __init__(self):
        # Embed the hostMap directly in the class
        self.host_map = {
            "UAT01": {
                "RCC": {
                    "STP": ["hosta","hostb"],
                    "WB":  ["hostc","hostd"]
                },
                "WSDC": {
                    "STP": ["hoste","hostf"],
                    "WB":  ["hostg","hosth"]
                }
            },
            "UAT02": {
                "RCC": {
                    "STP": ["hosti","hostj"],
                    "WB":  ["hostk","hostl"]
                },
                "WSDC": {
                    "STP": ["hostm","hostn"],
                    "WB":  ["hosto","hostp"]
                }
            }
        }

    def get_hosts(self, envs, site, comp):
        """
        envs: list of environments (e.g. ["UAT01","UAT02"])
        site: "RCC", "WSDC", or "ALL"/"BOTH"
        comp: "STP", "WB", or "STPWB"
        Returns: CSV string of hostnames
        """
        hosts = []

        for env in envs:
            env_map = self.host_map.get(env, {})

            # Sites to use
            sites_to_use = env_map.keys() if site in ("ALL", "BOTH") else [site]

            for s in sites_to_use:
                comp_map = env_map.get(s, {})

                # Components to use
                comps_to_use = ["STP","WB"] if comp == "STPWB" else [comp]

                for c in comps_to_use:
                    hosts.extend(comp_map.get(c, []))

        # Deduplicate + sort, then return as CSV
        return ",".join(sorted(set(hosts)))
