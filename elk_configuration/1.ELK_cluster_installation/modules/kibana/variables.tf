# ---------------------------------------------------------------------------------------------------------------------
# REQUIRED PARAMETERS
# Provide a value for each of these parameters.
# ---------------------------------------------------------------------------------------------------------------------

variable "instance_count" {
  description = "Number of instances"
  type        = string
}

variable "ami" {
  description = "AMI for instances"
  type        = string
}

variable "instance_type" {
  description = "Type of instance"
  type        = string
}
variable "subnet_id" {
  description = "The subnet_id for existed subnet"
  type        = string
}

variable "volume_type" {
  description = "Type of volume"
  type        = string
}
variable "volume_size" {
  description = "Size of volume"
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
