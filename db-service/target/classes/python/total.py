#引包
import requests
import time
#生成时间戳
def getTime():
    return int(round(time.time() * 1000))
#爬虫代码
if __name__ == '__main__':
    url='https://data.stats.gov.cn/easyquery.htm'
    headers={'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69'}
    key={}#参数键值对
    key['m']='QueryData'
    key['dbcode']='hgnd'
    key['rowcode']='zb'
    key['colcode']='sj'
    key['wds']='[]'
    key['dfwds']='[{"wdcode":"zb","valuecode":"A070E"},{"wdcode":"sj","valuecode":"2019"}]'
    key['k1']=str(getTime())
    requests.packages.urllib3.disable_warnings()
    r=requests.get(url,headers=headers,params=key,verify=False)
    print(r.json())
    #key['dfwds']='[{"wdcode":"sj","valuecode":"2019"}]'
    r.close()