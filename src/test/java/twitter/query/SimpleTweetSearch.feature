Feature: run a query

Background:

Scenario: The Query

# this url is for tweet query
Given url 'https://api.twitter.com/1.1/search/tweets.json'
And header Authorization = 'Bearer ' + accessToken
And header Accept-Encoding = 'gzip'
And param q = aT
And param count = counT
And param until = untiL
When method get
Then status 200