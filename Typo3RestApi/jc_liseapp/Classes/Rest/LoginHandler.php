<?php

/**
 * Created by PhpStorm.
 * User: daniel
 * Date: 01.04.14
 * Time: 21:55
 */
namespace LiseAppServer\Rest;
use Bullet\App;
use Cundd\Rest\Dispatcher;
use Cundd\Rest\Handler;
use Cundd\Rest\HandlerInterface;
use Cundd\Rest\Request;
require 'Classes/Managers/SecurityManager.php';
require 'Classes/DataInterfaces/UserAdapter.php';
/**
 * Handler for the credentials authorization
 *
 * @package Cundd\Rest\Handler
 */
class AuthHandler implements HandlerInterface {

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
	public function checkLogin($sentData) {
		$loginStatus = self::STATUS_LOGGED_OUT;
		if (isset($sentData['username']) && isset($sentData['$password'])) {
			$username = $sentData['username'];
			$password = $sentData['$password'];
			global $Security;
			return $Security.login($username,$password);
		}
		return FALSE;
	}
	/**
	 * Log out
	 *
	 * @return bool
	 */
	public function logout() {
		global $Security;
		return $Security.logout();
	}
	/**
	 * Configure the API paths
	 */
	public function configureApiPaths() {
		$dispatcher = Dispatcher::getSharedDispatcher();
		/** @var App $app */
		$app = $dispatcher->getApp();
		/** @var AuthHandler */
		$handler = $this;
		global $Security;
		$app->path($dispatcher->getPath(), function ($request) use ($handler, $app) {
			$handler->setRequest($request);
			$app->path('login', function($request) use ($handler, $app) {
				$getCallback = function ($request) use ($handler) {
					$user = $Security->getUsername();
					return $user!=NULL&&$user!='';
				};
				$app->get($getCallback);
				$loginCallback = function ($request) use ($handler) {
					$dispatcher = Dispatcher::getSharedDispatcher();
					return $handler->checkLogin($dispatcher->getSentData());
				};
				$app->post($loginCallback);
			});
			$app->path('logout', function($request) use ($handler, $app) {
				$getCallback = function ($request) use ($handler) {
					return $handler->logout();
				};
				$app->get($getCallback);
			});
		});
	}
} 