terraform {
  required_version = ">= 0.12, < 0.13"
}

# Create sonarqube virtual machine
resource "aws_instance" "sonarqube" {
  count         = var.instance_count
  ami           = var.ami
  instance_type = var.instance_type
  key_name = "jenkins_aws"
  subnet_id = var.subnet_id
  vpc_security_group_ids = [aws_security_group.sonarqube_sg.id]
  private_ip = "172.31.32.10${count.index + 1}"
  root_block_device  {
      
      volume_type = var.volume_type
      volume_size = var.volume_size
      delete_on_termination = true
      tags = {
        Name = "sonarqube${count.index + 1}"
      }
    }

  tags = {
    Name = "sonarqube${count.index + 1}"
    Environment = "${var.environment}"
  }
}

resource "aws_route53_record" "sonarqube_${count.index + 1}" {
  zone_id = data.aws_route53_zone.${var.dns_zone}.zone_id
  name    = "sonarqube${count.index + 1}.${var.dns_name}"
  type    = "A"
  ttl     = "300"
  records = [aws_instance.elk_${count.index + 1}.private_ip]
}

# create and associate elastic IP
resource "aws_eip" "sonarqube_ip" {
  vpc = true
}
resource "aws_eip_association" "sonarqube_assoc" {
  instance_id   = aws_instance.sonarqube.id
  allocation_id = aws_eip.sonarqube_ip.id
}
