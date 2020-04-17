job "thidwick" {
  datacenters = ["devtest-denver"]
  type = "service"
  region = "denver"

  # The "group" stanza defines a series of tasks that should be co-located on
  # the same Nomad client. Any task within a group will be placed on the same
  # client.
  group "thidwick" {

    # The "count" parameter specifies the number of the task groups that should
    # be running under this group. This value must be non-negative and defaults
    # to 1.
    count = 1

    task "thidwick-server" {

      driver = "docker"

      # The "config" stanza specifies the driver configuration, which is passed
      # directly to the driver to start the task. The details of configurations
      # are specific to each driver, so please see specific driver
      # documentation for more information.
      config {
        image = "osvc-docker-local.dockerhub-den.oraclecorp.com/dev-environments/thidwick-base"
        force_pull = true
        port_map {
          http = 8080
        }
      }

      env {
        JAVA_OPTS = "-Xms1024m -Xmx1024m -Dconsul.base.address=https://nomad.service.devtest-denver.consul.lan:8500"
      }

      logs {
        max_files     = 10
        max_file_size = 15
      }

      resources {
        memory = 1536
        network {
          mbits = 20
          port "http" {}
        }
      }

      service {
        name = "thidwick-service"
        tags = [
            "haproxy-service",
            "dns-prefix:thidwick"
        ]
        port = "http"
        check {
          type     = "http"
          path     = "/admin/ping"
          interval = "10s"
          timeout  = "2s"
        }
      }

    }
  }
}
