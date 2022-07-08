all		:
	(cd bin; make)
	(. ./bin/ai_profile ; cd adapters; make)

clean: 
	(cd bin; make clean)
	(cd adapters; make clean)
	rm -rf classes/*
	rm -rf logs/*
