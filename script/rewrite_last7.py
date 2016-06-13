import sys
inputdir
filter=int(sys.argv[3])
def onefile(input,output):
    fin=open(sys.argv[1],"r")
    fout=open(sys.argv[2],"w")

count=0
for line in fin:
    count+=1
    if(count>filter)
    fout.write(line)
fout.close()
fin.close()