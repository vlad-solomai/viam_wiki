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
# Create ELK_hosts SG
resource "aws_security_group" "elk_sg" {
  vpc_id       = data.aws_vpc.${var.working_vpc}.id
  name         = "${var.environment} ELK SG"
  description  = "${var.environment} ELK SG"
  
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
  tags = {
    Name = "${var.environment} ELK SG"
  }
}
