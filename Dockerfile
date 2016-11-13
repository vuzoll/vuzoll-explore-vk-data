FROM node:latest

RUN npm install -g json-server

EXPOSE 3000
ADD data/exploration-report.json /exploration-report.json
ENTRYPOINT ["json-server", "exploration-report.json"]
CMD []
