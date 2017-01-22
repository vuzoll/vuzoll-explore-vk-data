FROM openjdk:8

RUN mkdir -p /usr/src/vuzoll-explore-vk-data
RUN mkdir -p /usr/app

COPY build/distributions/* /usr/src/vuzoll-explore-vk-data/

RUN unzip /usr/src/vuzoll-explore-vk-data/vuzoll-explore-vk-data-*.zip -d /usr/app/
RUN ln -s /usr/app/vuzoll-explore-vk-data-* /usr/app/vuzoll-explore-vk-data

WORKDIR /usr/app/vuzoll-explore-vk-data

ENTRYPOINT ["./bin/vuzoll-explore-vk-data"]
CMD []
