$Id: readme.txt 943 2011-08-10 14:48:33Z abm65@FED.CCLRC.AC.UK $

1. Build the software with the following command: ant

2. Change the PATH environmental variable to find icat_cmd.sh: export PATH=$PATH:scripts

3. Test icat_cmd_util with the following command and expect the result 141: icat_cmd.sh -l | wc -l

4. Test with a more realistic command: icat_cmd.sh createInvestigation --title test2 --invNumber 4323 --invType experiment

- the end -

