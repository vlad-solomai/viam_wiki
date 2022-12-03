terraform {
  required_version = ">= 0.12, < 0.13"
}

# -----------------------------------------VPC configuration-----------------------------------------------
# Get exist data from AWS
data "aws_availability_zones" "${var.working_zone}" {}
data "aws_vpc" "${var.working_vpc}" {
  tags = {
    Name = "${var.working_vpc}"
  }
}

# ----------------------------------------------DNS configuration------------------------------------------
data "aws_route53_zone" "${var.dns_zone}" {
  name         = "${var.dns_name}"
  private_zone = true
}

# ----------------------------------------------Security Group configuration-------------------------------
# Create SG
resource "aws_security_group" "sonarqube_sg" {
  vpc_id       = data.aws_vpc.${var.working_vpc}.id
  name         = "${var.environment} SQ SG"
  description  = "${var.environment} SQ SG"
  
  # allow ingress of all private subnets
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["${var.private_cidr_block}"]
  } 

  # allow egress of all ports
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # allow ingress from zabbix
    # cidr_blocks = ["subnet/mask"]
  # allow ingress from bastion
    # cidr_blocks = ["subnet/mask"]
  # allow ingress from proxy01-test
    # cidr_blocks = ["subnet/mask"]
  # allow ingress from jenkins
    # cidr_blocks = ["subnet/mask"]

  tags = {
    Name = "${var.environment} SQ SG"
  }
}
