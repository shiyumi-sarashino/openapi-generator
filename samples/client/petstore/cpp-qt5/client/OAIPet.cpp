/**
 * OpenAPI Petstore
 * This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


#include "OAIPet.h"

#include "OAIHelpers.h"

#include <QJsonDocument>
#include <QJsonArray>
#include <QObject>
#include <QDebug>

namespace OpenAPI {

OAIPet::OAIPet(QString json) {
    this->init();
    this->fromJson(json);
}

OAIPet::OAIPet() {
    this->init();
}

OAIPet::~OAIPet() {

}

void
OAIPet::init() {
    m_id_isSet = false;
    m_id_isValid = false;
    m_category_isSet = false;
    m_category_isValid = false;
    m_name_isSet = false;
    m_name_isValid = false;
    m_photo_urls_isSet = false;
    m_photo_urls_isValid = false;
    m_tags_isSet = false;
    m_tags_isValid = false;
    m_status_isSet = false;
    m_status_isValid = false;
}

void
OAIPet::fromJson(QString jsonString) {
    QByteArray array (jsonString.toStdString().c_str());
    QJsonDocument doc = QJsonDocument::fromJson(array);
    QJsonObject jsonObject = doc.object();
    this->fromJsonObject(jsonObject);
}

void
OAIPet::fromJsonObject(QJsonObject json) {
    m_id_isValid = ::OpenAPI::fromJsonValue(id, json[QString("id")]);
    
    m_category_isValid = ::OpenAPI::fromJsonValue(category, json[QString("category")]);
    
    m_name_isValid = ::OpenAPI::fromJsonValue(name, json[QString("name")]);
    
    
    m_photo_urls_isValid = ::OpenAPI::fromJsonValue(photo_urls, json[QString("photoUrls")]);
    
    m_tags_isValid = ::OpenAPI::fromJsonValue(tags, json[QString("tags")]);
    m_status_isValid = ::OpenAPI::fromJsonValue(status, json[QString("status")]);
    
}

QString
OAIPet::asJson () const {
    QJsonObject obj = this->asJsonObject();
    QJsonDocument doc(obj);
    QByteArray bytes = doc.toJson();
    return QString(bytes);
}

QJsonObject
OAIPet::asJsonObject() const {
    QJsonObject obj;
	if(m_id_isSet){
        obj.insert(QString("id"), ::OpenAPI::toJsonValue(id));
    }
	if(category.isSet()){
        obj.insert(QString("category"), ::OpenAPI::toJsonValue(category));
    }
	if(m_name_isSet){
        obj.insert(QString("name"), ::OpenAPI::toJsonValue(name));
    }
	
    if(photo_urls.size() > 0){
        obj.insert(QString("photoUrls"), ::OpenAPI::toJsonValue(photo_urls));
    } 
	
    if(tags.size() > 0){
        obj.insert(QString("tags"), ::OpenAPI::toJsonValue(tags));
    } 
	if(m_status_isSet){
        obj.insert(QString("status"), ::OpenAPI::toJsonValue(status));
    }
    return obj;
}

qint64
OAIPet::getId() const {
    return id;
}
void
OAIPet::setId(const qint64 &id) {
    this->id = id;
    this->m_id_isSet = true;
}

OAICategory
OAIPet::getCategory() const {
    return category;
}
void
OAIPet::setCategory(const OAICategory &category) {
    this->category = category;
    this->m_category_isSet = true;
}

QString
OAIPet::getName() const {
    return name;
}
void
OAIPet::setName(const QString &name) {
    this->name = name;
    this->m_name_isSet = true;
}

QList<QString>
OAIPet::getPhotoUrls() const {
    return photo_urls;
}
void
OAIPet::setPhotoUrls(const QList<QString> &photo_urls) {
    this->photo_urls = photo_urls;
    this->m_photo_urls_isSet = true;
}

QList<OAITag>
OAIPet::getTags() const {
    return tags;
}
void
OAIPet::setTags(const QList<OAITag> &tags) {
    this->tags = tags;
    this->m_tags_isSet = true;
}

QString
OAIPet::getStatus() const {
    return status;
}
void
OAIPet::setStatus(const QString &status) {
    this->status = status;
    this->m_status_isSet = true;
}

bool
OAIPet::isSet() const {
    bool isObjectUpdated = false;
    do{ 
        if(m_id_isSet){ isObjectUpdated = true; break;}
    
        if(category.isSet()){ isObjectUpdated = true; break;}
    
        if(m_name_isSet){ isObjectUpdated = true; break;}
    
        if(photo_urls.size() > 0){ isObjectUpdated = true; break;}
    
        if(tags.size() > 0){ isObjectUpdated = true; break;}
    
        if(m_status_isSet){ isObjectUpdated = true; break;}
    }while(false);
    return isObjectUpdated;
}

bool
OAIPet::isValid() const {
    // only required properties are required for the object to be considered valid
    return m_name_isValid && m_photo_urls_isValid && true;
}

}

