<?php
namespace LiseAppServer\Managers;
use TYPO3\CMS\Core\SingletonInterface;

class SessionManager{
	const KEY_PREFIX = 'LiseApp-';
	/**
	 * @var bool
	 */
	protected $didInitialize = FALSE;
	/**
	 * Reads the session data from the database
	 */
	protected function _initialize() {
		if (!$this->didInitialize) {
			$GLOBALS['TSFE']->fe_user->fetchSessionData();
			$this->didInitialize = TRUE;
		}
	}
	/**
	 * Returns the value for the given key
	 *
	 * @param string $key
	 * @return mixed
	 */
	public function valueForKey($key) {
		$this->_initialize();
		return $GLOBALS['TSFE']->fe_user->getKey('ses', self::KEY_PREFIX . $key);
	}
	/**
	 * Sets the value for the given key
	 *
	 * @param string $key
	 * @param mixed  $value
	 * @return $this
	 */
	public function setValueForKey($key, $value) {
		$GLOBALS['TSFE']->fe_user->setKey('ses', self::KEY_PREFIX . $key, $value);
		$GLOBALS['TSFE']->fe_user->storeSessionData();
		return $this;
	}
}
$Session = new SessionManager();
