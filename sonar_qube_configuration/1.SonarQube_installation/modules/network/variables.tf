# ---------------------------------------------------------------------------------------------------------------------
# REQUIRED PARAMETERS
# Provide a value for each of these parameters.
# ---------------------------------------------------------------------------------------------------------------------

variable "working_zone" {
  description = "Enter the name of existed working zone"
  type        = string
}

variable "working_vpc" {
  description = "Enter the name of existed working vpc"
  type        = string
}

variable "private_cidr_block" {
  description = "The cidr_block for private subnet"
  type        = string
}

variable "environment" {
  description = "The name of environment"
  type        = string
}

variable "dns_zone" {
  description = "Enter the name of existed dns zone"
  type        = string
}
variable "dns_name" {
  description = "Enter the name of existed domain name"
  type        = string
}
