class HostResolver:
    def __init__(self):
        # Embed the hostMap directly in the class
        self.host_map = {
            "UAT01": {
                "RCC": {
                    "STP": ["uat01_rcc_stp01","uat01_rcc_stp02"],
                    "WB":  ["uat01_rcc_wb01"]
                },
                "WSDC": {
                    "STP": ["uat01_wsdc_stp01","uat01_wsdc_stp02"],
                    "WB":  ["uat01_wsdc_wb01"]
                }
            },
            "UAT02": {
                "RCC": {
                    "STP": ["uat02_rcc_stp01"],
                    "WB":  ["uat02_rcc_wb01"]
                },
                "WSDC": {
                    "STP": ["uat02_wsdc_stp01"],
                    "WB":  ["uat02_wsdc_wb01","uat02_wsdc_wb02"]
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
    def get_hosts(self, envs, site, comp):
        # If envs is a string, split on commas
        if isinstance(envs, str):
            envs = [e.strip() for e in envs.split(",")]
        hosts = []
        for env in envs:
            env_map = self.host_map.get(env, {})
            sites_to_use = env_map.keys() if site in ("ALL", "BOTH") else [site]
            for s in sites_to_use:
                comp_map = env_map.get(s, {})
                comps_to_use = ["STP","WB"] if comp == "STPWB" else [comp]
                for c in comps_to_use:
                    hosts.extend(comp_map.get(c, []))
        return ",".join(sorted(set(hosts)))
