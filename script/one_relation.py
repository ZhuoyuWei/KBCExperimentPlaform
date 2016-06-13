import sys

f=open(sys.argv[1])
epoch=int(sys.argv[2])

fout=open(sys.argv[3],"w")
sum=float(sys.argv[4])

count=0
for line in f:
    ss=line.strip().split()
    if len(ss)!=8:
        continue
    #fout.write(str(count)+"\t"+ss[7]+"\n")
    fout.write(str(count)+":"+str(float(ss[7])/(sum))+",")
    count+=1
    if count==epoch:
        break

f.close()
fout.close()