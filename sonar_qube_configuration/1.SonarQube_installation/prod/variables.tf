variable "aws_region" {
  default = "eu-west-2"
}

variable "ami" {
  description = "ami for instances"
  type        = string
  default     = "ami-04f5641b0d178a27a"
}

variable "working_zone" {
  default = "working"
}
variable "working_vpc" {
  default = "default_vpc"
}
variable "private_cidr_block" {
  default = "172.32.0.0/16"
}
variable "environment" {
  default = "prod"
}
variable "dns_zone" {
  default = "prod_zone"
}
variable "dns_name" {
  default = "prod.com"
}

variable "instance_count" {
  type = map

  default = {
    sonarqube_instance_count = "1"
  }
}
variable "instance_type" {
  type = map

  default = {
    sonarqube_instance_type = "t3.xlarge"
  }
}
variable "subnet_id" {
  default = "subnet-db7e7cawdf"
}
variable "volume_type" {
  type = map

  default = {
    sonarqube_volume_type = "gp2"
  }
}
variable "volume_size" {
  type = map

  default = {
    sonarqube_volume_size = "50"
  }
}
