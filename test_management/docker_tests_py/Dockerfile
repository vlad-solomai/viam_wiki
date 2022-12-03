FROM @DOCKER_REGISTRY@/base/python351-trusty:latest as base

# Exalink requirements
LABEL platform="@PLATFORM@"
LABEL service_type="@service_type@"
LABEL service_name="@service_name@"
LABEL version_major="@version_major@"
LABEL version_minor="@version_minor@"

# Python requirements
ENV PIP_TRUSTED_HOST=@PIP_TRUSTED_HOST@
ENV PIP_INDEX_URL=@PIP_INDEX_URL@
ENV PIP_EXTRA_INDEX_URL=@PIP_EXTRA_INDEX_URL@

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

# Make a workdir
COPY . /app
WORKDIR /app

# Install packages
RUN set -x \
    && rm -rf /app/tests/* \
    && pip3.5 install -U pip setuptools \
    && pip3.5 install -e . \
    && chmod +x docker-entrypoint.sh

ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["runserver"]

#############################################################################

FROM base as test

WORKDIR /app
ADD tests/* /app/tests/

RUN set -x \
    && pip3.5 install flake8 coverage pytest \
    && python3.5 $(which flake8) --exclude=".svn,CVS,.bzr,.hg,.git,__pycache__,.tox,.eggs,*.egg,venv/" --ignore=E501 || custom_exit_code=1 \
    && python3.5 $(which coverage) run $(which pytest) -v --ignore=venv/ --ignore=/usr --junitxml=/app/make_test_results.xml tests/ || custom_exit_code=1 \
    && python3.5 $(which coverage) xml --omit=venv/*,/usr/* -o "/app/make_coverage_results.xml" || custom_exit_code=1 \
    && python3.5 $(which coverage) report --omit=venv/*,/usr/* || custom_exit_code=1 \
    && tar -cvf reports.tar make_coverage_results.xml make_test_results.xml \
    && echo ${custom_exit_code:-0}

CMD ["cat", "reports.tar"]
