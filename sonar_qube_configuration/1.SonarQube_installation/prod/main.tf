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

module "sonarqube" {
  source = "../modules/sonarqube"

  instance_count = var.sonarqube_instance_count
  ami            = var.ami
  instance_type  = var.sonarqube_instance_type
  subnet_id      = var.subnet_id
  volume_type    = var.sonarqube_volume_type
  volume_size    = var.sonarqube_volume_size
  environment    = var.environment
  dns_zone       = var.dns_zone
  dns_name       = var.dns_name
}
