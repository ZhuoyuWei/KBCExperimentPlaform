import sys
import math

relnum=int(sys.argv[2])
epoach=int(sys.argv[3])

readdata=[]
for i in range(relnum):
    readdata.append([])
    for j in range(6):
        readdata[i].append([])
        for k in range(7):
            readdata[i][j].append(0)

for i in range(relnum):
    try:
        f=open(sys.argv[1]+str(i)+'log','r')
    except Exception as e:
        continue
    tmplines=[]
    for line in f:
        line=line.strip()
        tmplines.append(line)
    #print 'tmplist '+str(len(tmplines))+" "+str(i)
    if len(tmplines)<7+epoach:
        continue
    l=len(tmplines)
    for j in range(epoach+1,epoach+6):
        ss=tmplines[j].split('\t')
        if len(ss) != 7:
            print 'error'
        for k in range(7):
            #print float(ss[k])
            a=float(ss[k])
            #print a
            if not math.isnan(a):
                try:
                    readdata[i][j-epoach-1][k]=a
                except Exception as e:
                    print str(i)+" "+str(j-1)+" "+str(k)
                    print e
                    sys.exit(-1)
            
#print readdata
            
sum=[]
for i in range(6):
    sum.append([])
    for j in range(7):
        sum[i].append(0)

for r in range(relnum):
    for i in range(6):
        for j in range(6):
            sum[i][j]+=readdata[r][i][j]*readdata[r][i][6]
triplet_size=0
for r in range(relnum):
    triplet_size+=readdata[r][0][6]
      
for i in range(6):
    for j in range(6):
        sum[i][j]/=triplet_size
        sys.stdout.write(str(sum[i][j])+'\t')
    sys.stdout.write('\n')