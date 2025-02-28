./gradlew clean build
java -jar build/quarkus-app/quarkus-run.jar

sudo docker build -f src/main/docker/Dockerfile.jvm -t quarkus/nbody-jvm .
sudo docker run -i --rm -p 8080:8080 quarkus/nbody-jvm


git remote add dokku dokku@cluster-ig4.igpolytech.fr:#APP_NAME
git add . && git commit -m "changement pour deployement" && git push
git push dokku master:master
# websocket
npm install -g wscat
wscat -c ws://localhost:8080/click-client
