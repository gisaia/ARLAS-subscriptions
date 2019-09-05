#!/usr/bin/env python3

import logging
import os
import subprocess
import sys

import docker
import lxml.etree

"""This script publishes Docker images for components `subscription-manager` & `subscription-matcher`.

Prerequisites:

- `sudo pip3 install docker lxml`
- Being authenticated to repository `gisaia` on Docker Hub 
"""


script_dir = os.path.dirname(os.path.abspath(__file__))
project_root_dir = os.path.dirname(script_dir)

logging.basicConfig(level=logging.INFO)
docker_client = docker.from_env()

########################################################################################################################
# Functions
########################################################################################################################


def extract_version():
    pom_path = os.path.join(project_root_dir, 'pom.xml')
    pom_xml_root = lxml.etree.parse(source=pom_path).getroot()
    pom_xml_namespace = pom_xml_root.nsmap[None]
    return pom_xml_root.find('{{{}}}version'.format(pom_xml_namespace)).text


version = extract_version()


def publish_image(dockerfile_name, repo):
    latest_image_name = repo + ':latest'
    versioned_image_name = repo + ':' + version

    logging.info(msg="Build versioned Docker image {}".format(versioned_image_name))
    image, build_logs = docker_client.images.build(
        path=project_root_dir,
        tag=versioned_image_name,
        pull=True,
        dockerfile=dockerfile_name
    )

    logging.info(msg="Push versioned Docker image {}".format(versioned_image_name))
    docker_client.images.push(repository=repo, tag=version)

    logging.info(msg="Tag latest Docker image {}".format(latest_image_name))
    image.tag(repository=repo, tag='latest')

    logging.info(msg="Push latest Docker image {}".format(latest_image_name))
    docker_client.images.push(repository=repo, tag='latest')


########################################################################################################################
# Script
########################################################################################################################

logging.info(msg="Clean")
subprocess.run(
    ["sudo", "mvn", "clean"],
    stdout=sys.stdout,
    stderr=subprocess.STDOUT,
    timeout=60,
    check=True,
)

logging.info(msg="Package binaries")
subprocess.run(
    ["mvn", "package"],
    stdout=sys.stdout,
    stderr=subprocess.STDOUT,
    timeout=300,
    check=True,
)

publish_image('Dockerfile-manager', 'gisaia/arlas-subscription-manager')
publish_image('Dockerfile-matcher', 'gisaia/arlas-subscription-matcher')
