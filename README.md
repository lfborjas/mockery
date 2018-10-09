# XML Mockery

A little XML service that stands in for a vendor (and an example of a small Clojure API using [Liberator](https://clojure-liberator.github.io/liberator/))

## Usage

This API currently exposes only one endpoint, `/card_service`, that responds to POST requests sending XML request bodies, and returns XML responses. The vendor being replicated specifies its own request format, which is the following for most actions taken on a "card":

```xml
<?xml version="1.0" encoding="UTF‐8"?>
<TransferredValueTxn>
    <TransferredValueTxnReq> 
        <ReqCat>TransferredValue</ReqCat> 
        <ReqAction>Redeem</ReqAction> 
        <Date>20040327</Date> 
        <Time>221522</Time> 
        <PartnerName>MOCK</PartnerName>
        <CardActionInfo>
            <PIN>12340066</PIN>
            <AcctNum>MOCKCLIENT</AcctNum>
            <SrcRefNum>123412341234</SrcRefNum>
        </CardActionInfo>
    </TransferredValueTxnReq>
</TransferredValueTxn>
```

The actions the API knows how to respond to are (see the linked templates for the response format, anything in `{{double brackets}}` is variable data that the server will populate with sensible values):

* [Echo](https://github.com/lfborjas/mockery/blob/master/resources/card_responses/echo.xml) (returns whatever you send in the `EchoData` node)
* [StatInq](https://github.com/lfborjas/mockery/blob/master/resources/card_responses/card-action.xml)
* [Redeem](https://github.com/lfborjas/mockery/blob/master/resources/card_responses/card-action.xml)
* [RedeemReversal](https://github.com/lfborjas/mockery/blob/master/resources/card_responses/card-action.xml)


For `StatInq`, `Redeem` and `RedeemReversal`, if you send a `CardActionInfo/PIN` with the following use-case-specific **last 4 digits**, the response won't be success, but the specified message (notice that any suffix in the `:all` category will apply in any case, whereas the others depend on the `ReqAction` in the response):

```clj
  {:stat-inq        {:4001 "Card Is Active"
                     :4002 "Card Is Deactive"
                     :4003 "Card Is Redeemed"
                     :4006 "Card Not Found"
                     :4007 "Card Expired"}
   :redeem          {:0000 "Success"
                     :0043 "Card Is Invalid"
                     :0046 "Card Is Deactive"
                     :0038 "Card Is Redeemed"}
   :redeem-reversal {:0000 "Success"
                     :0067 "Not Reversible"}
   :all             {:0014 "Routing Error"
                     :0016 "Database Error"
                     :0019 "PIN Not found"
                     :0020 "Invalid PIN"
                     :0022 "MDN Not Found"
                     :0035 "Server Error"
                     :0036 "Server Error"
                     :0037 "Server Error"
                     :0082 "Invalid Identity Number"}}
```

For any of the three non-echo actions, sending the `PIN` `1234666` will return success, but it'll have different product info (useful if you want to see a different successful action).

For any action, sending a malformed XML request, specifying an unknown action, or failing to send a PIN for the card actions, a [bad request](https://github.com/lfborjas/mockery/blob/master/resources/card_responses/bad-request.xml) XML document will be returned.

Due to the semantics of POST, a status of `201 Created` will be returned in all cases when an XML request has been sent and content negotiation doesn't fail. The appropriate HTTP headers will be returned in any other case (e.g. if you send an `Accept` header of `text/html`, you'll receive a `406` response with a blank body).

Here's a poorly screenshot Postman session to illustrate a proper request and its response:

![image](https://user-images.githubusercontent.com/82133/46646039-e20d9600-cb55-11e8-981d-542dbb605f8a.png)

![image](https://user-images.githubusercontent.com/82133/46646049-f05bb200-cb55-11e8-969b-c3a778e7464c.png)



## Development

This project uses the [lein-ring](https://github.com/weavejester/lein-ring) plugin, which adds a couple of helpful Leiningen tasks:

* To run the server, run `lein ring server`
* To generate a standalone jar, run `lein ring uberjar`

### Deploying to Heroku

You'll have to make sure to [override the build environment variable](https://stackoverflow.com/questions/37962402/how-to-configure-build-command-in-herokus-clojure-buildpack): `heroku config:set LEIN_BUILD_TASK="ring uberjar"`--notice that the Procfile in this project relies on an uberjar generated with that particular command, the regular task (`uberjar`) will not generate the right `main` method!
