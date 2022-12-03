terraform {
  required_version = ">= 0.12, < 0.13"
}

# Create logstash virtual machine
resource "aws_instance" "logstash" {
  count         = var.instance_count
  ami           = var.ami
  instance_type = var.instance_type
  key_name = "jenkins_aws"
  subnet_id = var.subnet_id
  vpc_security_group_ids = [aws_security_group.elk_sg.id]
  private_ip = "172.31.32.12${count.index + 1}"
  root_block_device  {
      
      volume_type = var.volume_type
      volume_size = var.volume_size
      delete_on_termination = true
      tags = {
        Name = "logstash${count.index + 1}"
      }
    }

  tags = {
    Name = "logstash${count.index + 1}"
    Environment = "${var.environment}"
  }
}

resource "aws_route53_record" "logstash_${count.index + 1}" {
  zone_id = data.aws_route53_zone.${var.dns_zone}.zone_id
  name    = "logstash${count.index + 1}.${var.dns_name}"
  type    = "A"
  ttl     = "300"
  records = [aws_instance.logstash_${count.index + 1}.private_ip]
}
