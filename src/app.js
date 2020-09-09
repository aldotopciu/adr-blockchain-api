'use strict';

const bodyParser = require('body-parser');
const util = require('util');
const path = require('path');
const fs = require('fs');

let network = require('./fabric/network.js');

const configPath = path.join(process.cwd(), '../config.json');
const configJSON = fs.readFileSync(configPath, 'utf8');
const config = JSON.parse(configJSON);

//use this identity to query
const appAdmin = config.appAdmin;
let response;

var args = process.argv.slice(2);
console.log(args[0]);

switch (args[0]){
  case 'transfer':
    response = transfer(args);
    break;
  case 'transferMultiple':
    response = transferMultiple(args[1]);
    break;
  case 'getWork':
    response = getWork(args[1]);
    break;
  case 'getWallet':
    response = getWallet(args[1]);
    break;
  case 'getWalletsWithTokenAmountGreaterThan':
    response = getWalletsWithTokenAmountGreaterThan(args[1]);
    break;
  case 'createWork':
    response = createWork(args[1],args[2]);
    break;
  case 'registerUser':
    response = registerUser(args[1],args[2],args);
    break;
  default:
    response = "No function with that name!"
    break;
}

//get all the wallets with token amount greater than
 async function getWalletsWithTokenAmountGreaterThan (amount) {
  let networkObj = await network.connectToNetwork(appAdmin);
  let response = await network.invoke(networkObj, true, 'getWalletsWithTokenAmountGreaterThan', amount);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
}

//get Work by isrc
 async function getWork(isrc) {
  let networkObj = await network.connectToNetwork(appAdmin);
  let response = await network.invoke(networkObj, true, 'getWork', isrc);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
}

//get Wallet by email
 async function getWallet (email) {
  let networkObj = await network.connectToNetwork(appAdmin);
  let response = await network.invoke(networkObj, true, 'getWallet', email);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
}

//create Work asset
 async function createWork (userId,arg) {
  var args = arg.split("^");
  let networkObj = await network.connectToNetwork(userId);
  console.log('util inspecting');
  console.log(util.inspect(networkObj));
  let response = await network.invoke(networkObj, false, 'createWork', args);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
  }

 async function transfer (args) {
  let networkObj = await network.connectToNetwork(appAdmin);
  console.log('util inspecting');
  console.log(util.inspect(networkObj));
  let response = await network.invoke(networkObj, false, 'transfer', args);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
}

 async function transferMultiple (arg) {
  var args = arg.split('^');
  let networkObj = await network.connectToNetwork(appAdmin);
  console.log('util inspecting');
  console.log(util.inspect(networkObj));
  let response = await network.invoke(networkObj, false, 'transferMultiple', args);
  if (response.error) {
    return "Error";
  } else {
      return await JSON.parse(response);
  }
}

//get voter info, create voter object, and update state with their voterId
 async function registerUser (userId, balance , args) {
  //first create the identity for the voter and add to wallet
  let response = await network.registerUser(userId, balance);
  console.log('response from registerUser: ');
  console.log(response);
  if (response.error) {
    return "Error";
  } else {
    let networkObj = await network.connectToNetwork(userId);
    console.log('networkobj: ');
    console.log(networkObj);

    if (networkObj.error) {
      console.log(networkObj.error);
      return "Error";
    }
    console.log('network obj');
    console.log(util.inspect(networkObj));

    //connect to network and update the state with userId

    let invokeResponse = await network.invoke(networkObj, false, 'createWallet', userId, balance);

    if (invokeResponse.error) {
      console.log(invokeResponse.error);
      return "Error";
    } else {
      console.log('after network.invoke ');
      let parsedResponse = JSON.parse(invokeResponse);
      parsedResponse += '. Use userId to login above.';
      return parsedResponse;
    }
  }
}
