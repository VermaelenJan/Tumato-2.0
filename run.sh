source venv/bin/activate

cd dsl_webui
python application.py &

cd ../mission_dsl/
./gradlew jettyRun &

cd ../policy_generator
./gradlew startServer &

trap 'kill $(jobs -pr)' INT TERM

cat
