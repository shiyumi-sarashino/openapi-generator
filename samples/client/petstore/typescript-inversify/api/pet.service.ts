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
/* tslint:disable:no-unused-variable member-ordering */

import { Observable } from "rxjs/Observable";
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import IHttpClient from "../IHttpClient";
import { inject, injectable } from "inversify";
import { IAPIConfiguration } from "../IAPIConfiguration";
import { Headers } from "../Headers";
import HttpResponse from "../HttpResponse";

import { ApiResponse } from '../model/apiResponse';
import { Pet } from '../model/pet';

import { COLLECTION_FORMATS }  from '../variables';



@injectable()
export class PetService {
    private basePath: string = 'http://petstore.swagger.io/v2';

    constructor(@inject("IApiHttpClient") private httpClient: IHttpClient,
        @inject("IAPIConfiguration") private APIConfiguration: IAPIConfiguration ) {
        if(this.APIConfiguration.basePath)
            this.basePath = this.APIConfiguration.basePath;
    }

    /**
     * Add a new pet to the store
     * 
     * @param pet Pet object that needs to be added to the store
     
     */
    public addPet(pet: Pet, observe?: 'body', headers?: Headers): Observable<any>;
    public addPet(pet: Pet, observe?: 'response', headers?: Headers): Observable<HttpResponse<any>>;
    public addPet(pet: Pet, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!pet){
            throw new Error('Required parameter pet was null or undefined when calling addPet.');
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<any>> = this.httpClient.post(`${this.basePath}/pet`, pet , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <any>(httpResponse.response));
        }
        return response;
    }


    /**
     * Deletes a pet
     * 
     * @param petId Pet id to delete
     * @param apiKey 
     
     */
    public deletePet(petId: number, apiKey?: string, observe?: 'body', headers?: Headers): Observable<any>;
    public deletePet(petId: number, apiKey?: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<any>>;
    public deletePet(petId: number, apiKey?: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!petId){
            throw new Error('Required parameter petId was null or undefined when calling deletePet.');
        }

        if (apiKey) {
            headers['api_key'] = String(apiKey);
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/json';

        const response: Observable<HttpResponse<any>> = this.httpClient.delete(`${this.basePath}/pet/${encodeURIComponent(String(petId))}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <any>(httpResponse.response));
        }
        return response;
    }


    /**
     * Finds Pets by status
     * Multiple status values can be provided with comma separated strings
     * @param status Status values that need to be considered for filter
     
     */
    public findPetsByStatus(status: Array<'available' | 'pending' | 'sold'>, observe?: 'body', headers?: Headers): Observable<Array<Pet>>;
    public findPetsByStatus(status: Array<'available' | 'pending' | 'sold'>, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Pet>>>;
    public findPetsByStatus(status: Array<'available' | 'pending' | 'sold'>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!status){
            throw new Error('Required parameter status was null or undefined when calling findPetsByStatus.');
        }

        let queryParameters: string[] = [];
        if (status) {
            queryParameters.push("status="+encodeURIComponent(status.join(COLLECTION_FORMATS['csv'])));
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/xml';

        const response: Observable<HttpResponse<Array<Pet>>> = this.httpClient.get(`${this.basePath}/pet/findByStatus?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Pet>>(httpResponse.response));
        }
        return response;
    }


    /**
     * Finds Pets by tags
     * Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.
     * @param tags Tags to filter by
     
     */
    public findPetsByTags(tags: Array<string>, observe?: 'body', headers?: Headers): Observable<Array<Pet>>;
    public findPetsByTags(tags: Array<string>, observe?: 'response', headers?: Headers): Observable<HttpResponse<Array<Pet>>>;
    public findPetsByTags(tags: Array<string>, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!tags){
            throw new Error('Required parameter tags was null or undefined when calling findPetsByTags.');
        }

        let queryParameters: string[] = [];
        if (tags) {
            queryParameters.push("tags="+encodeURIComponent(tags.join(COLLECTION_FORMATS['csv'])));
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/xml';

        const response: Observable<HttpResponse<Array<Pet>>> = this.httpClient.get(`${this.basePath}/pet/findByTags?${queryParameters.join('&')}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Array<Pet>>(httpResponse.response));
        }
        return response;
    }


    /**
     * Find pet by ID
     * Returns a single pet
     * @param petId ID of pet to return
     
     */
    public getPetById(petId: number, observe?: 'body', headers?: Headers): Observable<Pet>;
    public getPetById(petId: number, observe?: 'response', headers?: Headers): Observable<HttpResponse<Pet>>;
    public getPetById(petId: number, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!petId){
            throw new Error('Required parameter petId was null or undefined when calling getPetById.');
        }

        // authentication (api_key) required
        if (this.APIConfiguration.apiKeys && this.APIConfiguration.apiKeys["api_key"]) {
            headers['api_key'] = this.APIConfiguration.apiKeys["api_key"];
        }
        headers['Accept'] = 'application/xml';

        const response: Observable<HttpResponse<Pet>> = this.httpClient.get(`${this.basePath}/pet/${encodeURIComponent(String(petId))}`, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <Pet>(httpResponse.response));
        }
        return response;
    }


    /**
     * Update an existing pet
     * 
     * @param pet Pet object that needs to be added to the store
     
     */
    public updatePet(pet: Pet, observe?: 'body', headers?: Headers): Observable<any>;
    public updatePet(pet: Pet, observe?: 'response', headers?: Headers): Observable<HttpResponse<any>>;
    public updatePet(pet: Pet, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!pet){
            throw new Error('Required parameter pet was null or undefined when calling updatePet.');
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/json';
        headers['Content-Type'] = 'application/json';

        const response: Observable<HttpResponse<any>> = this.httpClient.put(`${this.basePath}/pet`, pet , headers);
        if (observe == 'body') {
               return response.map(httpResponse => <any>(httpResponse.response));
        }
        return response;
    }


    /**
     * Updates a pet in the store with form data
     * 
     * @param petId ID of pet that needs to be updated
     * @param name Updated name of the pet
     * @param status Updated status of the pet
     
     */
    public updatePetWithForm(petId: number, name?: string, status?: string, observe?: 'body', headers?: Headers): Observable<any>;
    public updatePetWithForm(petId: number, name?: string, status?: string, observe?: 'response', headers?: Headers): Observable<HttpResponse<any>>;
    public updatePetWithForm(petId: number, name?: string, status?: string, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!petId){
            throw new Error('Required parameter petId was null or undefined when calling updatePetWithForm.');
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/json';

        let formData: FormData = new FormData();
        headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
        if (name !== undefined) {
            formData.append('name', <any>name);
        }
        if (status !== undefined) {
            formData.append('status', <any>status);
        }

        const response: Observable<HttpResponse<any>> = this.httpClient.post(`${this.basePath}/pet/${encodeURIComponent(String(petId))}`, body, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <any>(httpResponse.response));
        }
        return response;
    }


    /**
     * uploads an image
     * 
     * @param petId ID of pet to update
     * @param additionalMetadata Additional data to pass to server
     * @param file file to upload
     
     */
    public uploadFile(petId: number, additionalMetadata?: string, file?: Blob, observe?: 'body', headers?: Headers): Observable<ApiResponse>;
    public uploadFile(petId: number, additionalMetadata?: string, file?: Blob, observe?: 'response', headers?: Headers): Observable<HttpResponse<ApiResponse>>;
    public uploadFile(petId: number, additionalMetadata?: string, file?: Blob, observe: any = 'body', headers: Headers = {}): Observable<any> {
        if (!petId){
            throw new Error('Required parameter petId was null or undefined when calling uploadFile.');
        }

        // authentication (petstore_auth) required
        if (this.APIConfiguration.accessToken) {
            let accessToken = typeof this.APIConfiguration.accessToken === 'function'
                ? this.APIConfiguration.accessToken()
                : this.APIConfiguration.accessToken;
            headers['Authorization'] = 'Bearer ' + accessToken;
        }
        headers['Accept'] = 'application/json';

        let formData: FormData = new FormData();
        headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
        if (additionalMetadata !== undefined) {
            formData.append('additionalMetadata', <any>additionalMetadata);
        }
        if (file !== undefined) {
            formData.append('file', <any>file);
        }

        const response: Observable<HttpResponse<ApiResponse>> = this.httpClient.post(`${this.basePath}/pet/${encodeURIComponent(String(petId))}/uploadImage`, body, headers);
        if (observe == 'body') {
               return response.map(httpResponse => <ApiResponse>(httpResponse.response));
        }
        return response;
    }

}
