

# StockOrder
* Has a UUID id (see prefix comb uuids section below)
* Instrument
* State (Open / Closed). Once an order is closed no more trades can happen and it's a terminal state.
* Type Buy / Sell
* Target Quantity
* Target Price
* Fulfillments
* BasketId (CompositeOrder Id if there's a parent)


Fulfillments are an abstract concept that can be matching orders, or external liquidity ... etc. Each order can be fulfilled with multiple fulfillments,

Fulfillment have the following:
* Id
* State (MATCHED / REVERSED / EXECUTED)
* Fulfilled Price
* Fulfilled quanity
* Fulfilling Entity Id (i.e other order matching)


## Composite Order
Composite orders only have an ID, symbol and a list of underlying stock orders. 

## Matching Algorithm

* Maintain a pool for sell orders, whenever we get a buy order match it to the best possible price

* If we couldn't match we add the buy order to a list of pending buy orders


* When adding a sell order we first try to match it to any of the pending buy orders. If not we just add it to the pool


* We do not match partial instruments (part of buckets) together


* Matching algorithm takes request for matching and it's only responsible for generating pairs. It does not support things such as adding an order to the matching queue and then removing it. Because it would require having a stateful engine

## General Flow:

1- Order is created (CreateOrder)
2- Order publishes a matching request to the engine (RequestFulfillment) by sending a (RequestMatchingEvent)
3- Engine responds with a match event (FulfillmentMatchedEvent)
4- Order system matches the ordr (MatchOrders)
5- If the order still has pending quantity to fulfill it sends another matching request (RequestFulfillment)
6- if the order and the fulfiller (matching order) are able to execute, execute any pending trades (ExecuteFulfillments)
7- Price is updated (ExecuteTrade)


## Architecture

* The implementation uses a pool of threads to process events
* Events belonging to the same instrument are processed on the same thread
* This guarantees that first come is first served 
* In cloud setup with more consistency requirements the same could be achieved with solutions like kafka
