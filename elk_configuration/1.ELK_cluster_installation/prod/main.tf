terraform {
  required_version = ">= 0.12, < 0.13"
}

provider "aws" {
  region = var.aws_region

  # Allow any 2.x version of the AWS provider
  version = "~> 2.0"
}

terraform {
  backend "s3" {
    bucket         = "<YOUR S3 BUCKET>"
    key            = "<SOME PATH>/terraform.tfstate"
    region         = var.aws_region
    dynamodb_table = "<YOUR DYNAMODB TABLE>"
    encrypt        = true
  }
}

module "network" {
  source = "../modules/network"

  working_zone        = var.working_zone
  working_vpc         = var.working_vpc
  private_cidr_block  = var.private_cidr_block
  environment         = var.environment
  dns_zone            = var.dns_zone
  dns_name            = var.dns_name
}

module "elasticshearch" {
  source = "../modules/elasticshearch"

  instance_count = var.elasticshearch_instance_count
  ami            = var.ami
  instance_type  = var.elasticshearch_instance_type
  subnet_id      = var.subnet_id
  volume_type    = var.elasticshearch_volume_type
  volume_size    = var.elasticshearch_volume_size
  environment    = var.environment
  dns_zone       = var.dns_zone
  dns_name       = var.dns_name
}

module "kibana" {
  source = "../modules/kibana"

  instance_count = var.kibana_instance_count
  ami            = var.ami
  instance_type  = var.kibana_instance_type
  subnet_id      = var.subnet_id
  volume_type    = var.kibana_volume_type
  volume_size    = var.kibana_volume_size
  environment    = var.environment
  dns_zone       = var.dns_zone
  dns_name       = var.dns_name
}

module "network" {
  source = "../modules/logstash"

  instance_count = var.logstash_instance_count
  ami            = var.ami
  instance_type  = var.logstash_instance_type
  subnet_id      = var.subnet_id
  volume_type    = var.logstash_volume_type
  volume_size    = var.logstash_volume_size
  environment    = var.environment
  dns_zone       = var.dns_zone
  dns_name       = var.dns_name
}
