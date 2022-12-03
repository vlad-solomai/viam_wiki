output "vpc_id" {
  value = data.aws_vpc.${working_vpc}.id
}

output "availability_zone_name" {
    value = data.aws_availability_zones.${var.working_zone}.names[1]
}
