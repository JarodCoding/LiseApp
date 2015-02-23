<?php
/*
 *  Copyright notice
 *
 *  (c) 2014 Daniel Corn <info@cundd.net>, cundd
 *
 *  All rights reserved
 *
 *  This script is part of the TYPO3 project. The TYPO3 project is
 *  free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The GNU General Public License can be found at
 *  http://www.gnu.org/copyleft/gpl.html.
 *
 *  This script is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  This copyright notice MUST APPEAR in all copies of the script!
 */

/**
 * Created by PhpStorm.
 * User: daniel
 * Date: 01.04.14
 * Time: 21:55
 */

namespace JarodCoding\JcLiseapp\Rest;
require_once '/Security/SecurityManager.php';
require_once  '/DataInterfaces/UserAdapter.php';
require_once  '/DataInterfaces/NewsAdapter.php';
use Bullet\App;
use Cundd\Rest\Dispatcher;
use Cundd\Rest\HandlerInterface;
use Cundd\Rest\Request;
use LiseAppServer\Managers;
use LiseAppServer\DataInterface;
use LiseAppServer\Managers\SecurityManager;
use LiseAppServer\DataInterface\UserAdapter;
use LiseAppServer\DataInterface\NewsAdapter;

/**
 * Example handler
 *
 * @package Cundd\Rest\Handler
 */
class Handler implements HandlerInterface {
/**
	 * Current request
	 *
	 * @var Request
	 */
	protected $request;

	public function setRequest($request) {
		$this->request    = $request;
		return $this;
	}
	/**
	 * Returns the current request
	 *
	 * @return \Cundd\Rest\Request
	 */
	public function getRequest() {
		return $this->request;
	}

	/**
	 * Check the given login data
	 *
	 * @param array $sentData
	 * @return bool
	 */
	/**
	 * Configure the API paths
	 */
	public function configureApiPaths() {
		$dispatcher = Dispatcher::getSharedDispatcher();
		/** @var App $app */
		$app = $dispatcher->getApp();
		/** @var Handler */
		$handler = $this;
		$app->path($dispatcher->getPath(), function ($request) use ($handler, $app) {
			$handler->setRequest($request);
			$app->path('login', function($request) use ($handler, $app) {

				$loginCallback = function ($request) use ($handler, $app) {
				$dispatcher = Dispatcher::getSharedDispatcher();
				$data = $dispatcher->getSentData();
				try{
						 return $app->response(200,SecurityManager::getInstance()->login($data['username'], $data['password'], $data['nonce'], $data['timestamp']));
					}catch(\Exception $e){
						return $app->response(401,"Login failed: ".$e);
					}
					return null;
				};

				$app->post($loginCallback);
			});
			$app->path('news', function($request) use ($handler, $app) {
				
				$GetNews = function ($request) use ($handler, $app) {
					try{
						$err = SecurityManager::getInstance()->securityCheck();
					}catch(\Exception $e){
						$err = $e;
					}
					if($err != null)return $app->response(401, "Auto Authentication Error: ".$err);
					
					try{
						return NewsAdapter::getInstance()->listAllReadable(SecurityManager::getInstance()->username);
					}catch(\Exception $e){
						$err = $e;
					}
					if($err != null)return $app->response(401, "Error: ".$err);
						
				};
			
				$app->get($GetNews);
			});
			$app->path('test', function($request) use ($handler, $app) {
						
				$getCallback = function ($request) use ($handler, $app) {
					try{
					$err = SecurityManager::getInstance()->securityCheck();
					}catch(\Exception $e){
						$err = $e;
					}
					if($err != null)return $app->response(401, "Auto Authentication Error: ".$err);
				
					return array(
						'Result' => "Hello World!",
					);				};
				$app->get($getCallback);
				});
				
		});
	}

// 	/**
// 	 * Configure the API paths
// 	 */
// 	public function configureApiPaths() {
// 		$dispatcher = Dispatcher::getSharedDispatcher();

// 		/** @var App $app */
// 		$app = $dispatcher->getApp();

// 		/** @var Handler */
// 		$handler = $this;

// 		$app->path($dispatcher->getPath(), function ($request) use ($handler, $app, $dispatcher) {
// 			$handler->setRequest($request);

// 			# curl -X GET http://your-domain.com/rest/customhandler
// 			$app->get(function ($request) use ($dispatcher) {
// 				return array(
// 					'path' => $dispatcher->getPath(),
// 					'uri'  => $dispatcher->getUri(),
// 				);
// 			});

// 			$app->path('subpath', function ($request) use ($handler, $app, $dispatcher) {
// 				# curl -X GET http://your-domain.com/rest/customhandler/subpath
// 				$getCallback = function ($request) use ($handler, $dispatcher) {
// 					return array(
// 						'path' => $dispatcher->getPath(),
// 						'uri'  => $dispatcher->getUri(),
// 					);
// 				};
// 				$app->get($getCallback);

// 				# curl -X POST -d '{"username":"johndoe","password":"123456"}' http://your-domain.com/rest/customhandler/subpath
// 				$postCallback = function ($request) use ($handler) {
// 					$dispatcher = Dispatcher::getSharedDispatcher();
// 					return array(
// 						'path' => $dispatcher->getPath(),
// 						'uri'  => $dispatcher->getUri(),
// 						'data' => $dispatcher->getSentData(),
// 					);
// 				};
// 				$app->post($postCallback);
// 			});
// 		});
// 	}
}